package com.xakers.seminarmanager.memorypool;

/**
 * Manages a memory pool for efficient allocation and deallocation of memory.
 * The memory pool is represented as a byte array, with free blocks maintained
 * in a linked list structure for quick access.
 * 
 * @author Xavier Akers
 * @version 1.0, 2024-12-27
 * @since 2024-12-26
 */

public class MemManager {

	/**
	 * Represents a handle to a memory block within the pool. Contains the starting
	 * position and size of the allocated memory block.
	 */
	public class MemHandle {
		private int position; // Starting position of the memory block
		private int size; // Size of the memory block

		/**
		 * Constructs a memory handle with the specified position and size.
		 * 
		 * @param position The starting position of the memory block.
		 * @param size     The size of the memory block.
		 */
		public MemHandle(int position, int size) {
			this.position = position;
			this.size = size;
		}

		/**
		 * @return the position
		 */
		public int getPosition() {
			return position;
		}

		/**
		 * @return the size
		 */
		public int getSize() {
			return size;
		}

	}

	private byte[] pool; // The memory pool represented as a byte array
	private FreeBlock[] freeList; // Array of free block lists, indexed by block size
	private int poolSize; // Total size of the memory pool in bytes

	/**
	 * Constructs a memory manager with a specified pool size. Initializes the
	 * memory pool and sets up the free block list.
	 * 
	 * @param poolSize The size of the memory pool.
	 */
	public MemManager(int poolSize) {
		this.poolSize = poolSize;
		this.pool = new byte[poolSize];
		this.freeList = new FreeBlock[(int) (Math.log(poolSize) / Math.log(2))];
		this.freeList[this.freeList.length - 1] = new FreeBlock(0, poolSize);

	}

	/**
	 * Inserts serialized data into the memory pool. Finds a suitable free block for
	 * the data and copies the content into the pool.
	 * 
	 * @param byteSem  The serialized seminar data as a byte array.
	 * @param byteSize The size of the byte array.
	 * @return A handle to the memory block where the data was inserted.
	 * @throws IllegalStateException If no suitable free block is available.
	 */
	public MemHandle insert(byte[] byteSem, int byteSize) {
		// Check if freeList has space
		FreeBlock freeBlock = allocateBlock(byteSize);
		if (freeBlock == null) {
			throw new IllegalStateException("error : memory allocation failed no suitable block available.");
		}
		int blockIndex = calculateBlockIndex(freeBlock.getSize());

		// Removing freeBlock from the list
		removeBlockFromFreeList(blockIndex, freeBlock);

		// Copying in data
		System.arraycopy(byteSem, 0, this.pool, freeBlock.getPosition(), byteSize);
		return new MemHandle(freeBlock.getPosition(), byteSize);
	}

	/**
	 * Retrieves the byte information
	 * 
	 * @param space  Array pass by reference; where the bytes get returned
	 * @param handle The memHandle
	 * @return The length of the string
	 */
	public byte[] get(MemHandle handle) {
		byte[] buffer = new byte[handle.getSize()];
		System.arraycopy(this.pool, handle.getPosition(), buffer, 0, handle.getSize());
		return buffer;
	}

	/**
	 * Free a block at the position specified by the handle Merge adjacent free
	 * blocks
	 * 
	 * @param handle Memory Handle
	 */
	public void delete(MemHandle handle) {
		int blockSize = calculateTrueBlockSize(handle.getSize());

		int index = calculateBlockIndex(blockSize);
		FreeBlock freeBlock = new FreeBlock(handle.getPosition(), blockSize);

		// Insert the block into the appropriate free list
		insertIntoFreeList(index, freeBlock);

		// Attempt to merge with adjacent buddy blocks
		checkBuddies(freeBlock);
	}

	/**
	 * Print contents of FreeList
	 * 
	 */
	public void dump() {
		System.out.println("Freeblock List:");
		boolean isEmpty = true;

		for (int i = 0; i < freeList.length; i++) {
			if (freeList[i] != null) {
				System.out.printf("%d: %d", (int) Math.pow(2, i + 1), freeList[i].getPosition());
				isEmpty = false;

				FreeBlock printNext = freeList[i].getNext();
				while (printNext != null) {
					System.out.printf(" %d", printNext.getPosition());
					printNext = printNext.getNext();
				}
				System.out.println();
			}

		}
		if (isEmpty) {
			System.out.println("There are no freeblocks in the memory pool");
		}
	}

	/**
	 * Inserts a block into the free list at the specified index, maintaining order.
	 * 
	 * @param index    The index of the free list.
	 * @param newBlock The block to insert.
	 */
	private void insertIntoFreeList(int index, FreeBlock block) {
		if (freeList[index] == null) {
			freeList[index] = block; // Free list is empty, directly insert
			return;
		}
		FreeBlock curr = freeList[index];
		while (curr != null) {
			if (curr.getPosition() < block.getPosition()) {
				// Continue traversing until we find the correct position
				if (curr.getNext() != null) {
					curr = curr.getNext();
				} else {
					// Insert at the end of the list
					curr.setNext(block);
					block.setPrev(curr);
					return;
				}
			} else {
				// Insert before the current node
				if (curr.getPrev() != null) {
					curr.getPrev().setNext(block);
					block.setPrev(curr.getPrev());
				} else {
					// Insert at the beginning of the list
					freeList[index] = block;
				}
				curr.setPrev(block);
				block.setNext(curr);
				return;
			}
		}
	}

	/**
	 * Finds and creates space in the memory pool
	 * 
	 * @param byteSize The size of the bytes to be stored
	 * @return A FreeBlock object representing the allocated block
	 */
	private FreeBlock allocateBlock(int byteSize) {

		int requiredBlockSize = calculateTrueBlockSize(byteSize);
		FreeBlock freeBlock = findBlock(requiredBlockSize);

		// If no block is found, resize and try again
		while (freeBlock == null) {
			resize();
			freeBlock = findBlock(requiredBlockSize);
		}

		// Ensure the pool has enough space
		while (freeBlock.getSize() > requiredBlockSize) {
			freeBlock = splitBlock(freeBlock);
		}

		return freeBlock;
	}

	/**
	 * Finds the first suitable free block in the free list.
	 *
	 * @param requiredBlockSize The required block size.
	 * @return A FreeBlock object if found, otherwise null.
	 */
	private FreeBlock findBlock(int requiredBlockSize) {
		for (FreeBlock block : freeList) {
			while (block != null) {
				if (block.getSize() >= requiredBlockSize) {
					return block;
				}
				block = block.getNext();
			}
		}
		return null;
	}

	/**
	 * Removes a block from the free list at the specified index
	 * 
	 * @param blockIndex The index in the free list
	 * @param block      The block to remove
	 */
	private void removeBlockFromFreeList(int blockIndex, FreeBlock block) {
		if (freeList[blockIndex] == block) {
			freeList[blockIndex] = block.getNext();
		}
		if (block.getNext() != null) {
			block.getNext().setPrev(block.getPrev());
		}
		if (block.getPrev() != null) {
			block.getPrev().setNext(block.getNext());
		}
		block.setNext(null);
		block.setPrev(null);
	}

	/**
	 * Splits a freeBlock in half
	 * 
	 * @param block The available freeBlock
	 * @return the First half of the freeBlock
	 */
	private FreeBlock splitBlock(FreeBlock block) {

		// Split the block into two smaller blocks
		int halfSize = block.getSize() / 2;
		FreeBlock split1 = new FreeBlock(block.getPosition(), halfSize);
		FreeBlock split2 = new FreeBlock(block.getPosition() + halfSize, halfSize);

		// Link the two split blocks together
		split1.setNext(split2);
		split2.setPrev(split1);

		// Find the index in the freeList where the block is stored
		for (int i = 0; i < freeList.length; i++) {
			if (freeList[i] == block) {
				// Update the freeList by removing the current block
				if (block.getNext() != null) {
					freeList[i] = block.getNext();
					freeList[i].setPrev(null); // Fix link to the next block
				} else {
					freeList[i] = null; // Handle case when it's the last block in the list
				}

				// Add the first split block to the appropriate freeList position
				int splitIndex = (int) (Math.log(halfSize) / Math.log(2)) - 1;
				freeList[splitIndex] = split1; // Insert split1 into the appropriate index
				break;
			}
		}

		// Return the first split block
		return split1;
	}

	/**
	 * Resizes the memory pool by doubling its size. Allocates new memory pool,
	 * copies the existing data into the new pool, and updates the free block list
	 */
	private void resize() {
		// Double memory pool size
		this.poolSize *= 2;

		// Allocate new pool of the expanded size
		byte[] newPool = new byte[this.poolSize];

		// Only copy first half of old pool
		System.arraycopy(pool, 0, newPool, 0, this.pool.length);

		// Update reference
		this.pool = newPool;

		// Resize the freeList and add new free block
		FreeBlock[] newFreeList = new FreeBlock[freeList.length + 1];
		System.arraycopy(freeList, 0, newFreeList, 0, freeList.length);
		this.freeList = newFreeList;

		// Add the new free block corresponding to the new size
		FreeBlock newBlock = new FreeBlock(this.poolSize / 2, this.poolSize / 2);
		int newBlockIndex = freeList.length - 2;

		if (freeList[newBlockIndex] != null) {
			freeList[freeList.length - 2].setNext(newBlock);
			newBlock.setPrev(freeList[freeList.length - 2]);
		} else {
			freeList[newBlockIndex] = newBlock;
		}

		// Check if new block can be merged
		checkBuddies(newBlock);

		System.out.printf("Memory pool expanded to %d bytes%n", this.poolSize);
	}

	/**
	 * Merges memory blocks that are "buddies," i.e., they are adjacent in memory If
	 * merging is successful, the method recursively checks the resulting block's
	 * buddies.
	 * 
	 * @param block The free memory block to check
	 */
	private void checkBuddies(FreeBlock block) {
		int blockIndex = (int) ((Math.log(block.getSize()) / Math.log(2)) - 1);

		FreeBlock mergedBlock = null;

		// Check if next block is a buddy
		if (block.getNext() != null) {
			FreeBlock next = block.getNext();
			// Check if blocks are buddies based on their positions and sizes
			if ((block.getPosition() ^ block.getSize()) == next.getPosition()) {
				mergedBlock = new FreeBlock(block.getPosition(), block.getSize() * 2);

				// Update free list pointers
				if (block.getPrev() == null) {
					freeList[blockIndex] = next.getNext();
					if (freeList[blockIndex] != null) {
						freeList[blockIndex].setPrev(null);
					}
				} else {
					block.getPrev().setNext(next.getNext());
					if (next.getNext() != null) {
						next.getNext().setPrev(block.getPrev());
					}
				}
			}
		}

		// Check if the previous block is a buddy
		if (mergedBlock == null && block.getPrev() != null) {
			FreeBlock prev = block.getPrev();
			if ((prev.getPosition() ^ prev.getSize()) == block.getPosition()) {
				mergedBlock = new FreeBlock(prev.getPosition(), block.getSize() * 2);

				// Update free list pointers
				if (prev.getPrev() == null) {
					freeList[blockIndex] = block.getNext();
					if (freeList[blockIndex] != null) {
						freeList[blockIndex].setPrev(null);
					}
				} else {
					prev.getPrev().setNext(block.getNext());
					if (block.getNext() != null) {
						block.getNext().setPrev(prev.getPrev());
					}
				}
			}
		}

		// If blocks were merged, add merged block to the next tier and recursively
		// check for buddies
		if (mergedBlock != null) {
			int nextIndex = blockIndex + 1;

			mergedBlock.setNext(freeList[nextIndex]);
			if (freeList[nextIndex] != null) {
				freeList[nextIndex].setPrev(mergedBlock);
			}
			freeList[nextIndex] = mergedBlock;

			// Recursively check if the merged block can be merged further
			checkBuddies(mergedBlock);
		}
	}

	/**
	 * Calculates the true block size that should be allocated for the given byte
	 * size. The block size is rounded up to the nearest power of two
	 * 
	 * @param byteSize The requested byte size for allocation.
	 * @return The true block size, which is the smallest power of two that is
	 *         greater than or equal to byteSize.
	 */
	private int calculateTrueBlockSize(int byteSize) {
		// Ensure the byte size is a power of two and big enough for the allocation
		int trueBlockSize = 1;

		// Round up to the nearest power of two that fits the byteSize
		while (trueBlockSize < byteSize) {
			trueBlockSize *= 2;
		}
		return trueBlockSize;
	}

	/**
	 * Calculates the index in the free list for a given block size.
	 *
	 * @param blockSize The size of the block.
	 * @return The index in the free list.
	 */
	private int calculateBlockIndex(int blockSize) {
		return (int) (Math.log(blockSize) / Math.log(2)) - 1;
	}

}

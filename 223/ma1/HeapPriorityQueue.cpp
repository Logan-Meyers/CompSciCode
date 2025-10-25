#include "HeapPriorityQueue.h"
#include "PrinterJob.h"
#include <iostream>

// Constructor
HeapPriorityQueue::HeapPriorityQueue() {
    size = 0;
}

// Destructor
HeapPriorityQueue::~HeapPriorityQueue() {
    // Cleanup if necessary

    // nothing to clean up
}

// Copy constructor
HeapPriorityQueue::HeapPriorityQueue(const HeapPriorityQueue& other) {
    // Copy heap elements and size
    size = other.size;

    for (int i=0; i < size; i++)
        heap[i] = other.heap[i];
}

// Copy assignment operator
HeapPriorityQueue& HeapPriorityQueue::operator=(const HeapPriorityQueue& other) {
    // Assign heap elements and size properly
    size = other.size;

    for (int i=0; i < size; i++)
        heap[i] = other.heap[i];

    return *this;
}

// Enqueue function
void HeapPriorityQueue::enqueue(const string& str, int priority) {
    // Insert new PrinterJob(str, priority) while maintaining heap order using percolateUp

    // if size maxed out, return; enqueue will fail
    if (size == MAX_SIZE) return;

    cout << "Inserting at index: " << size << endl;

    // insert into back
    heap[size] = PrinterJob(str, priority);

    // percolate up
    percolateUp(size);

    // increase size
    size++;
}

// printJobs function
void HeapPriorityQueue::printJobs() {
    // Print and remove PrinterJobs from the heap in priority order and using percolateDown as necessary
    while (size > 0) {
        // print out top priority
        cout << heap[0].printString << "(Priority " << heap[0].priority << ")" << endl;

        // decrement size, and replace root with new last, and set last to nothing
        size--;
        heap[0] = heap[size];
        heap[size] = PrinterJob();

        // percolate the top value down into the tree to maintain property
        percolateDown(0);
    }
}

// percolateUp function
void HeapPriorityQueue::percolateUp(int index) {
    // Maintain heap order when inserting a new PrinterJob

    // base case: at root
    if (index == 0) return;

    // base case: parent < this
    if (heap[getParent(index)].priority <= heap[index].priority) return;

    // otherwise, swap this and parent
    cout << "Swapping indexes: this=" << index << " | parent=" << getParent(index) << endl;
    swap(heap[index], heap[getParent(index)]);

    // recursive call
    percolateUp(getParent(index));
}

// percolateDown function
void HeapPriorityQueue::percolateDown(int index) {
    // Maintain heap order when removing a PrinterJob

    // get left and right index, plus keep track of the index with smallest value
    int left = getLeft(index);
    int right = getRight(index);
    int smallest = index;  // default is the node we're currently at

    // check if left is in bounds and left < right
    if (left < size && heap[left].priority < heap[smallest].priority) {
        smallest = left;
    }

    // check if right is in bounds and right < left
    if (right < size && heap[right].priority < heap[smallest].priority) {
        smallest = right;
    }

    // swap this and the left/right child, then recurse down again
    if (smallest != index) {
        swap(heap[index], heap[smallest]);
        percolateDown(smallest);
    }
}

// ----- A few helper functions for indexing -----

// get the parent index of the given index
int HeapPriorityQueue::getParent(int index) {
    return floor((index-1) / 2);
}

// get the left node index of the given index
int HeapPriorityQueue::getLeft(int index) {
    return (2*index) + 1;
}

// get the right node index of the given index
int HeapPriorityQueue::getRight(int index) {
    return (2*index) + 2;
}

#include "HeapPriorityQueue.h"
#include "PrinterJob.h"
#include <iostream>

// TODO: Implement the constructor
HeapPriorityQueue::HeapPriorityQueue() {
    size = 0;
}

// TODO: Implement the destructor
HeapPriorityQueue::~HeapPriorityQueue() {
    // Cleanup if necessary

    // nothing to clean up
}

// TODO: Implement the copy constructor
HeapPriorityQueue::HeapPriorityQueue(const HeapPriorityQueue& other) {
    // Copy heap elements and size
}

// TODO: Implement the copy assignment operator
HeapPriorityQueue& HeapPriorityQueue::operator=(const HeapPriorityQueue& other) {
    // Assign heap elements and size properly
}

// TODO: Implement enqueue function
void HeapPriorityQueue::enqueue(const string& str, int priority) {
    // Insert new PrinterJob(str, priority) while maintaining heap order using percolateUp

    // if size maxed out, return; enqueue will fail
    if (size == MAX_SIZE) return;

    std::cout << "Inserting at index: " << size << std::endl;

    // insert into back
    heap[size] = PrinterJob(str, priority);

    // percolate up
    percolateUp(size);

    // increase size
    size++;
}

// TODO: Implement printJobs function
void HeapPriorityQueue::printJobs() {
    // Print and remove PrinterJobs from the heap in priority order and using percolateDown as necessary
    while (size > 0) {
        // print highest priority (root)
        std::cout << heap[0].printString << "(Priority " << heap[0].priority << ")" << std::endl;

        // replace root with bottom level right most node
        heap[0] = heap[size-1];
        heap[size-1] = PrinterJob();  // set last to default PrinterJob

        // percolate down
        percolateDown(0);

        // reduce size
        size--;
    }
}

// TODO: Implement percolateUp function
void HeapPriorityQueue::percolateUp(int index) {
    // Maintain heap order when inserting a new PrinterJob

    // base case: at root
    if (index == 0) return;

    // base case: parent < this
    if (heap[getParent(index)].priority <= heap[index].priority) return;

    // otherwise, swap this and parent
    std::cout << "Swapping indexes: this=" << index << " | parent=" << getParent(index) << std::endl;
    std::swap(heap[index], heap[getParent(index)]);

    // recursive call
    percolateUp(getParent(index));
}

// TODO: Implement percolateDown function
void HeapPriorityQueue::percolateDown(int index) {
    // Maintain heap order when removing a PrinterJob

    // base case: parent is smaller or equal to both children
    if (heap[index].priority <= heap[getLeft(index)].priority && heap[index].priority <= heap[getRight(index)].priority)
        return;

    // base case: at leaf node
    if (heap[getLeft(index)].priority == 0 && heap[getRight(index)].priority == 0)
        return;

    // otherwise, swap root and smallest child
    // get smallest child (that isn't null)
    int toSwapIndex = -1;

    if (heap[getLeft(index)].priority < heap[getRight(index)].priority && heap[getLeft(index)].priority > 0)
        toSwapIndex = getLeft(index);
    else if (heap[getRight(index)].priority < heap[getLeft(index)].priority && heap[getRight(index)].priority > 0)
        toSwapIndex = getRight(index);
    else
        return;  // something went wrong

    // recursive call
    percolateDown(toSwapIndex);
}

// ----- A few helper functions for indexing -----

int HeapPriorityQueue::getParent(int index) {
    return floor((index-1) / 2);
}

int HeapPriorityQueue::getLeft(int index) {
    return (2*index) + 1;
}

int HeapPriorityQueue::getRight(int index) {
    return (2*index) + 2;
}
#include <iostream>
#include <cassert>
#include <vector>

class MinHeap
{
private:
    std::vector<int> heap;

    int parent(int i) { return (i - 1) / 2; }
    int leftChild(int i) { return (2 * i) + 1; }
    int rightChild(int i) { return (2 * i) + 2; }

    void percolateUp(int index)
    {
        while (index > 0 && heap[index] < heap[parent(index)])
        {
            std::swap(heap[index], heap[parent(index)]);
            index = parent(index);
        }
    }

    // CAUSE OF BUG: This percolate down function was incorrectly percolating, which was
    //               causing the wrong value to be placed at the top when a remove occurs
    void percolateDown(int index)
    {
        int size = heap.size();

        while (leftChild(index) < size)
        {
            int left = leftChild(index);
            int right = rightChild(index);
            int targetChild = left;

            // THE FIX: Change the '>' to '<' when comparing heap[right]
            //          and heap[left] to correctly choose the next child to swap with
            if (right < size && heap[right] < heap[left])
            {
                targetChild = right;
            }

            if (heap[index] > heap[targetChild])
            {
                std::swap(heap[index], heap[targetChild]);
                index = targetChild;
            }
            else
            {
                break;
            }
        }
    }

public:
    MinHeap() {}

    void insert(int value)
    {
        heap.push_back(value);
        percolateUp(heap.size() - 1);
    }

    int removeMin()
    {
        assert(!heap.empty());

        int minValue = heap[0];
        heap[0] = heap.back();
        heap.pop_back();

        if (!heap.empty())
        {
            percolateDown(0);
        }

        return minValue;
    }

    bool isEmpty()
    {
        return heap.empty();
    }

    int size()
    {
        return heap.size();
    }
};

void testMinHeap()
{
    MinHeap pq;

    pq.insert(50);
    pq.insert(30);
    pq.insert(20);
    pq.insert(15);
    pq.insert(10);
    pq.insert(8);
    pq.insert(16);

    assert(pq.removeMin() == 8);
    assert(pq.removeMin() == 10);  // BUG: Next value to be popped should be 10, since 8 < 10 < 15
    assert(pq.removeMin() == 15);
    std::cout << "Test passed: Removed minimum value 8 then 10 then 15" << std::endl;
    // The above test is insufficient do more asserts
}

int main()
{
    testMinHeap();
    return 0;
}
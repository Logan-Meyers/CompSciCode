#include <iostream>
#include <cassert>

class Node
{
public:
    int data;
    Node *next;

    Node(int value) : data(value), next(nullptr) {}
};

class LinkedList
{
private:
    Node *head;

public:
    LinkedList() : head(nullptr) {}

    // Insert a node at the beginning
    void insert(int value)
    {
        Node *newNode = new Node(value);
        newNode->next = head;
        head = newNode;
    }

    // Search for a value in the list (just verify if its in the list)
    // Returns true if found, false if not found
    bool search(int value)
    {
        // CAUSE OF BUG: This search function simply visits next nodes until it finds the value requested,
        //               but that causes a seg fault (invalid memory access) if the value is not in the list

        Node *current = head;

        while (current != nullptr)  // THE FIX: search until nullptr found
        {
            if (current->data == value) return true;  // THE FIX: only return true if current's value is equal
            
            current = current->next;
        }

        return false;  // THE FIX: new default return of false
    }

    // Destructor to free all nodes
    ~LinkedList()
    {
        Node *current = head;
        while (current != nullptr)
        {
            Node *temp = current;
            current = current->next;
            delete temp;
        }
    }
};

void testLinkedList()
{
    LinkedList list;

    list.insert(10);
    list.insert(20);
    list.insert(30);

    // Test case - this passes, so add more till the bug is revealed and then fix the bug
    assert(list.search(10) == true);
    assert(list.search(20) == true);
    assert(list.search(30) == true);
    assert(list.search(50) == false);  // BUG: searching for a value which doe not exist in the list gives a seg fault!
    std::cout << "Test passed: Found 10, 20, and 30 in the list but not 50" << std::endl;
}

int main()
{
    testLinkedList();
    return 0;
}
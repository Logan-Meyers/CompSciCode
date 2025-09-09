#include <iostream>

using namespace std;

class IntBox {
private:
    int value;
public:
    IntBox(int v) : value(v) {}
    int getValue() const {return value; }
    void setValue(int v) { value = v; }
};

// then make use of templates to make Box generic to any type
template<typename T>
class Box {
    private:
    T value;
    public:
    Box(T v) : value(v) {}
    T getValue() const { return value; }
    void setValue(T newVal) { value = newVal; }
};

int main() {
    Box<int> a(3);

    printf("%d", a.getValue());

    return 0;
}
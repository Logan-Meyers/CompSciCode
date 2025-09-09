#include <iostream>
#include <stack>

// 스택의 요소 합을 계산하는 재귀 함수
int sumStack(std::stack<int>& s) {
    if (s.empty()) return 0;
    int 최상단값 = s.top(); // 최상단값 변수: 스택의 최상단 값 저장
    s.pop();
    int 합계 = 최상단값 + sumStack(s); // 합계 변수: 현재 값과 나머지 합 저장
    s.push(최상단값); // 스택을 복원하여 이후에 사용할 수 있도록 함
    return 합계;
}

int main() {
    std::stack<int> 숫자들; // 숫자들 변수: 정수를 저장하는 스택
    // 예시: 스택에 몇 개의 숫자를 추가합니다
    숫자들.push(1);
    숫자들.push(2);
    숫자들.push(3);
    숫자들.push(4);
    // 숫자들.push(5);

    int 전체합 = sumStack(숫자들); // 전체합 변수: 스택 요소의 합 저장
    std::cout << "스택 요소의 합: " << 전체합 << std::endl;

    return 0;
}
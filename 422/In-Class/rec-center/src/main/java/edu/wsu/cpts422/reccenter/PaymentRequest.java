package edu.wsu.cpts422.reccenter;

/** What the front desk form sends when a member pays. */
public class PaymentRequest {

    private String memberName;
    private Integer age;
    private String state = "WA";
    private Plan plan = Plan.MONTHLY;
    private PaymentMethod method = PaymentMethod.CASH;
    private String cardNumber;

    public PaymentRequest() {
    }

    public PaymentRequest(String memberName, Integer age, String state, Plan plan,
                          PaymentMethod method, String cardNumber) {
        this.memberName = memberName;
        this.age = age;
        this.state = state;
        this.plan = plan;
        this.method = method;
        this.cardNumber = cardNumber;
    }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
}

package entities;


public class Message {
    private String name;
    private String status;
    private String action;
    private String memberList;

    public Message() {
    }

    public Message(String name, String status, String action,String memberList) {
        this.name =name;
        this.status=status;
        this.action=action;
        this.memberList=memberList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    public String  getMemberList() {
        return memberList;
    }

    public void setMemberList(String memberList) {
        this.memberList = memberList;
    }

    @Override
    public String toString() {
        return "Message{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", action='" + action + '\'' +
                ", memberList='" + memberList + '\'' +
                '}';
    }
}


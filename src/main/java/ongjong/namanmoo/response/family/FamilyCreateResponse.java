package ongjong.namanmoo.response.family;

public class FamilyCreateResponse {

    public String status;
    public String message;
    public String inviteCode;

    public FamilyCreateResponse(String status, String message, String inviteCode) {
        this.status = status;
        this.message = message;
        this.inviteCode = inviteCode;
    }
}
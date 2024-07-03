package ongjong.namanmoo.response.family;

public class FamilyJoinResponse {

    public String status;
    public String message;
    public String familyId;

    public FamilyJoinResponse(String status, String message, String familyId) {
        this.status = status;
        this.message = message;
        this.familyId = familyId;
    }

}

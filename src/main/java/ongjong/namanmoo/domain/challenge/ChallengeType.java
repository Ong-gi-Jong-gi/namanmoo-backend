package ongjong.namanmoo.domain.challenge;

public enum ChallengeType {
    NORMAL, GROUP_CHILD, GROUP_PARENT, FACETIME, PHOTO, VOICE1, VOICE2, VOICE3, VOICE4;

    public int getVoiceTypeOrdinal() {
        switch (this) {
            case VOICE1:
                return 1;
            case VOICE2:
                return 2;
            case VOICE3:
                return 3;
            case VOICE4:
                return 4;
            default:
                return this.ordinal();
        }
    }

}
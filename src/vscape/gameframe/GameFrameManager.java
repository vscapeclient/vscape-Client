package vscape.gameframe;

public final class GameFrameManager {

    public enum GameFrameUI {
        DEFAULT(new DefaultFrame()),
        CLASSIC(new ClassicFrame());

        private GameFrame frame;
        GameFrameUI(GameFrame f) {
            frame = f;
        }

        public GameFrame getFrame() {
            return frame;
        }
    }

    private static GameFrameUI currentUI = GameFrameUI.DEFAULT;

    public static void init() {
        for (GameFrameUI data : GameFrameUI.values()) {
            if (data.getFrame() != null) {
                data.getFrame().init();
            }
        }
    }

    public static void setFrameUI(GameFrameUI newFrame) {
        currentUI = newFrame;
    }

    public static GameFrameUI getFrameUI() {
        return currentUI;
    }

    public static GameFrame getFrame() {
        return currentUI.getFrame();
    }

    public static boolean isCurrentUI(GameFrameUI toCheck) {
        return currentUI.equals(toCheck);
    }
}

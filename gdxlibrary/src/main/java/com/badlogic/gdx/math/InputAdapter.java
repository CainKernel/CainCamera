package com.badlogic.gdx.math;

/**
 * @author ferrisXu
 * 创建日期：2019/2/26
 * 描述：
 */
/** An adapter class for {@link InputProcessor}. You can derive from this and only override what you are interested in.
 *
 * @author mzechner */
public class InputAdapter implements InputProcessor {
    public boolean keyDown (int keycode) {
        return false;
    }

    public boolean keyUp (int keycode) {
        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved (int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled (int amount) {
        return false;
    }
}

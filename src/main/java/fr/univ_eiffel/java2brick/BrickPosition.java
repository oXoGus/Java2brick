package fr.univ_eiffel.java2brick;

import java.util.Objects;

// public record BrickPosition(Brick brick,int x,int y,int direction){}

public class BrickPosition {
    private final Brick brick;
    private final int x;
    private final int y;
    private final int direction;

    public BrickPosition(Brick brick, int x, int y, int direction) {
        Objects.requireNonNull(brick);

        if (x < 0) {
            throw new IllegalArgumentException("the x must be positive");
        }
        if (y < 0) {
            throw new IllegalArgumentException("the y must be positive");
        }
        if (direction < 0 || direction > 3) {
            throw new IllegalArgumentException("direction must be between 0 and 3");
        }

        this.brick = brick;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public Brick brick() {
        return brick;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int direction() {
        return direction;
    }
}

package com.qiscus.sdk.chat.core.data.model.urlsextractor;

public class Dimension {

    private Integer width = 0;
    private Integer height = 0;

    public Dimension(Integer width, Integer height)
    {
        this.width = width;
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    @Override
    public String toString() {
        return "Dimension{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
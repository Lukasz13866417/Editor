package org.example.editor.layout;

import java.util.ArrayList;
import java.util.List;

public class LayoutItem {
    public String id;              // unique
    // geom data relative to parent
    public double x, y;
    public double width, height;
    public List<LayoutItem> children = new ArrayList<>();
}

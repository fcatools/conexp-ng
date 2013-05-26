package com.eugenkiss.conexp2.draw;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.tudresden.inf.tcs.fcalib.utils.ListSet;

public class Node {

    private List<String> objects;
    private List<String> attributes;
    private int x;
    private int y;

    /**
     *
     * @param objects
     * @param attributes
     * @param x
     * @param y
     */
    public Node(List<String> objects, List<String> attributes, int x, int y) {
        this.objects = objects;
        this.attributes = attributes;
        this.x = x;
        this.y = y;
    }

    public Node(){
        this.objects = new ArrayList<>();
        this.attributes = new ArrayList<>();
        this.x = 0;
        this.y = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void addObject(String s){
        objects.add(s);
    }

    public void addAttribut(String s){
        attributes.add(s);
    }

}

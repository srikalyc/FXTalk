/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fxtalk.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.PerspectiveTransform;

/**
 *
 * @author srikalyanchandrashekar
 */
public class PerspespectiveUtil {
    
    private double width = 340;    //bind Main.scene.width;
    private double height = 200;   //bind Main.scene.height;
    private ObjectProperty<PerspectiveTransform> pT = new SimpleObjectProperty<PerspectiveTransform>();
    
    public PerspespectiveUtil(double w, double h) {
        this.width = w;
        this.height = h;
        pT.set(new PerspectiveTransform());
    }

    
    //Flips the node horizontally
    public void applyHorizontalPT(int t) {

        double radius = width/2;
        double back = height/5;
        PerspectiveTransform pt =  pT.get();
        if (t > 0) {        //This is for face facing front side
            pt.setUlx(radius - Math.sin(t)*radius);
            pt.setUrx(radius + Math.sin(t)*radius);
            pt.setLrx(radius + Math.sin(t)*radius);
            pt.setLlx(radius - Math.sin(t)*radius);
            pt.setUly(0 - Math.cos(t)*back);
            pt.setUry(0 + Math.cos(t)*back);
            pt.setLry(height - Math.cos(t)*back);
            pt.setLly(height + Math.cos(t)*back);
        } else if(t < 0) {  //This is for face facing back side
            pt.setUlx(radius + Math.sin(t)*radius);
            pt.setUrx(radius - Math.sin(t)*radius);
            pt.setLrx(radius - Math.sin(t)*radius);
            pt.setLlx(radius + Math.sin(t)*radius);
            pt.setUly(Math.cos(t)*back);
            pt.setUry(-Math.cos(t)*back);
            pt.setLry(height + Math.cos(t)*back);
            pt.setLly(height - Math.cos(t)*back);
        }
        pT.setValue(pt);//To ensure bound value is reflected.
    }
    //Flips the node vertically
    public void  applyVerticalPT(int  t) {
        double width = 340;    //bind Main.scene.width;
        double height = 200;   //bind Main.scene.height;
        double radius = height/2;
        double back = width/20;
        PerspectiveTransform pt =  pT.get();
        if (t > 0) {        //This is for face facing front side
            pt.setUlx(0 - Math.cos(t)*back);
            pt.setUrx(width + Math.cos(t)*back);
            pt.setLrx(width - Math.cos(t)*back);
            pt.setLlx(Math.cos(t)*back);
            pt.setUly(radius - Math.sin(t)*radius);
            pt.setUry(radius - Math.sin(t)*radius);
            pt.setLry(radius + Math.sin(t)*radius);
            pt.setLly(radius + Math.sin(t)*radius);
        } else if(t < 0) {  //This is for face facing back side
            pt.setUlx(0 + Math.cos(t)*back);
            pt.setUrx(width - Math.cos(t)*back);
            pt.setLrx(width + Math.cos(t)*back);
            pt.setLlx(-Math.cos(t)*back);
            pt.setUly(radius + Math.sin(t)*radius);
            pt.setUry(radius + Math.sin(t)*radius);
            pt.setLry(radius - Math.sin(t)*radius);
            pt.setLly(radius - Math.sin(t)*radius);
        }
        pT.setValue(pt);//To ensure bound value is reflected.
    }    
}

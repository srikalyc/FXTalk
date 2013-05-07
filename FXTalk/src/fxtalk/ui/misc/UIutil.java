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

package fxtalk.ui.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 * Simple helper classes.
 * @author srikalyc
 */
public class UIutil {
    public static Image getImage(String image) {
        Image dummy = new Image(getRsrcStream(image));
        if (dummy != null) {//To make sure we make the image smooth
            return new Image(getRsrcStream(image), dummy.getWidth(), dummy.getHeight(), true, true);
        }
        return new Image(getRsrcStream(image));
    }

    public static Image getImageFullUrl(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            try {
                return new Image(new FileInputStream(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UIutil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Image(imagePath);
    }

    public static InputStream getRsrcStream(String rsc) {
        return UIutil.class.getResourceAsStream("/fxtalk/assets/" + rsc);
    }

    public static URL getRsrcURI(String rsc) {
        return UIutil.class.getResource("/fxtalk/assets/" + rsc);
    }
}

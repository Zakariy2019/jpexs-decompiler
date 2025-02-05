/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.ConditionalType;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class FILLSTYLE implements NeedsCharacters, FieldChangeObserver, Serializable {

    @SWFType(BasicType.UI8)
    @EnumValue(value = SOLID, text = "Solid")
    @EnumValue(value = LINEAR_GRADIENT, text = "Linear gradient")
    @EnumValue(value = RADIAL_GRADIENT, text = "Radial gradient")
    @EnumValue(value = FOCAL_RADIAL_GRADIENT, text = "Focal radial gradient", minSwfVersion = 8)
    @EnumValue(value = REPEATING_BITMAP, text = "Repeating bitmap")
    @EnumValue(value = CLIPPED_BITMAP, text = "Clipped bitmap")
    @EnumValue(value = NON_SMOOTHED_REPEATING_BITMAP, text = "Non smoothed repeating bitmap")
    @EnumValue(value = NON_SMOOTHED_CLIPPED_BITMAP, text = "Non smoothed clipped bitmap")
    public int fillStyleType;

    public static final int SOLID = 0x0;

    public static final int LINEAR_GRADIENT = 0x10;

    public static final int RADIAL_GRADIENT = 0x12;

    @SWFVersion(from = 8)
    public static final int FOCAL_RADIAL_GRADIENT = 0x13;

    public static final int REPEATING_BITMAP = 0x40;

    public static final int CLIPPED_BITMAP = 0x41;

    @SWFVersion(from = 7)
    public static final int NON_SMOOTHED_REPEATING_BITMAP = 0x42;

    @SWFVersion(from = 7)
    public static final int NON_SMOOTHED_CLIPPED_BITMAP = 0x43;

    @Internal
    public boolean inShape3;

    @Conditional(value = "fillStyleType", options = {SOLID})
    @ConditionalType(type = RGBA.class, tags = {DefineShape3Tag.ID, DefineShape4Tag.ID})
    public RGB color;

    @Conditional(value = "fillStyleType", options = {LINEAR_GRADIENT, RADIAL_GRADIENT, FOCAL_RADIAL_GRADIENT})
    public MATRIX gradientMatrix;

    @Conditional(value = "fillStyleType", options = {LINEAR_GRADIENT, RADIAL_GRADIENT, FOCAL_RADIAL_GRADIENT})
    @ConditionalType(value = "fillStyleType", type = FOCALGRADIENT.class, options = {FOCAL_RADIAL_GRADIENT})
    public GRADIENT gradient;

    @Conditional(value = "fillStyleType", options = {REPEATING_BITMAP, CLIPPED_BITMAP, NON_SMOOTHED_REPEATING_BITMAP, NON_SMOOTHED_CLIPPED_BITMAP})
    public int bitmapId;

    @Conditional(value = "fillStyleType", options = {REPEATING_BITMAP, CLIPPED_BITMAP, NON_SMOOTHED_REPEATING_BITMAP, NON_SMOOTHED_CLIPPED_BITMAP})
    public MATRIX bitmapMatrix;

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        if ((fillStyleType == REPEATING_BITMAP)
                || (fillStyleType == CLIPPED_BITMAP)
                || (fillStyleType == NON_SMOOTHED_REPEATING_BITMAP)
                || (fillStyleType == NON_SMOOTHED_CLIPPED_BITMAP)) {
            if (bitmapId == 65535) { //In some cases, this special value is used, but is not used. Ignore it. (#1851)
                return;
            }
            CharacterTag character = swf.getCharacter(bitmapId);
            if (character instanceof ImageTag) {
                needed.add(bitmapId);
            }
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        if (bitmapId == oldCharacterId) {
            bitmapId = newCharacterId;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        if (bitmapId == characterId) {
            if ((fillStyleType == REPEATING_BITMAP)
                    || (fillStyleType == CLIPPED_BITMAP)
                    || (fillStyleType == NON_SMOOTHED_REPEATING_BITMAP)
                    || (fillStyleType == NON_SMOOTHED_CLIPPED_BITMAP)) {
                fillStyleType = SOLID;
                if (color == null) {
                    color = inShape3 ? new RGBA(0, 0, 0, 0) : new RGB(0, 0, 0);
                }
            }
            bitmapId = 0;
            return true;
        }
        return false;
    }

    @Override
    public void fieldChanged(Field field) {
        if ((fillStyleType == FILLSTYLE.LINEAR_GRADIENT)
                || (fillStyleType == FILLSTYLE.RADIAL_GRADIENT)) {
            if (gradient instanceof FOCALGRADIENT) {
                GRADIENT g = new GRADIENT();
                g.spreadMode = gradient.spreadMode;
                g.interpolationMode = gradient.interpolationMode;
                g.gradientRecords = gradient.gradientRecords;
                gradient = g;
            }
        } else if (fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
            if (!(gradient instanceof FOCALGRADIENT)) {
                FOCALGRADIENT g = new FOCALGRADIENT();
                g.spreadMode = gradient.spreadMode;
                g.interpolationMode = gradient.interpolationMode;
                g.gradientRecords = gradient.gradientRecords;
                gradient = g;
            }
        }
    }

    public boolean hasBitmap() {
        switch (fillStyleType) {
            case CLIPPED_BITMAP:
            case NON_SMOOTHED_CLIPPED_BITMAP:
            case NON_SMOOTHED_REPEATING_BITMAP:
            case REPEATING_BITMAP:
                return true;
        }
        return false;
    }

    public boolean hasGradient() {
        switch (fillStyleType) {
            case LINEAR_GRADIENT:
            case RADIAL_GRADIENT:
            case FOCAL_RADIAL_GRADIENT:
                return true;
        }
        return false;
    }

    public boolean isCompatibleFillStyle(FILLSTYLE otherFillStyle, SWF swf) {
        if (fillStyleType != otherFillStyle.fillStyleType) {
            return false;
        }
        switch (fillStyleType) {
            case CLIPPED_BITMAP:
            case NON_SMOOTHED_CLIPPED_BITMAP:
            case NON_SMOOTHED_REPEATING_BITMAP:
            case REPEATING_BITMAP:
                if (bitmapId != otherFillStyle.bitmapId) {
                    ImageTag imgThis = swf.getImage(bitmapId);
                    ImageTag imgOther = swf.getImage(otherFillStyle.bitmapId);
                    return imgThis.isSameImage(imgOther);
                }
                break;
            case LINEAR_GRADIENT:
            case RADIAL_GRADIENT:
            case FOCAL_RADIAL_GRADIENT:
                if (!gradient.isCompatibleGradient(otherFillStyle.gradient)) {
                    return false;
                }
                break;
        }
        return true;
    }

    public MORPHFILLSTYLE toMorphStyle() {
        MORPHFILLSTYLE morphFillStyle = new MORPHFILLSTYLE();
        morphFillStyle.bitmapId = bitmapId;
        if (bitmapMatrix != null) {
            morphFillStyle.startBitmapMatrix = new MATRIX(bitmapMatrix);
            morphFillStyle.endBitmapMatrix = new MATRIX(bitmapMatrix);
        }
        if (color != null) {
            morphFillStyle.startColor = new RGBA(color);
            morphFillStyle.endColor = new RGBA(color);
        }
        morphFillStyle.fillStyleType = fillStyleType;
        if (gradient != null) {
            morphFillStyle.startGradientMatrix = gradientMatrix;
            morphFillStyle.endGradientMatrix = gradientMatrix;
            morphFillStyle.gradient = gradient.toMorphGradient();
        }

        return morphFillStyle;
    }

    public MORPHFILLSTYLE toMorphStyle(FILLSTYLE endFillStyle, SWF swf) {
        if (!isCompatibleFillStyle(endFillStyle, swf)) {
            return null;
        }
        MORPHFILLSTYLE morphFillStyle = new MORPHFILLSTYLE();
        morphFillStyle.bitmapId = bitmapId;
        if (bitmapMatrix != null) {
            morphFillStyle.startBitmapMatrix = new MATRIX(bitmapMatrix);
        }
        if (endFillStyle.bitmapMatrix != null) {
            morphFillStyle.endBitmapMatrix = new MATRIX(endFillStyle.bitmapMatrix);
        }
        if (color != null) {
            morphFillStyle.startColor = new RGBA(color);
        }
        if (endFillStyle.color != null) {
            morphFillStyle.endColor = new RGBA(endFillStyle.color);
        }
        morphFillStyle.fillStyleType = fillStyleType;
        if (gradient != null) {
            morphFillStyle.startGradientMatrix = gradientMatrix;
            morphFillStyle.endGradientMatrix = endFillStyle.gradientMatrix;
            morphFillStyle.gradient = gradient.toMorphGradient(endFillStyle.gradient);
        }

        return morphFillStyle;
    }
}

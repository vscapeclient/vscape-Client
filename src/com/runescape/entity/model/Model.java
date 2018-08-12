package com.runescape.entity.model;

import com.runescape.Client;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.anim.FrameBase;
import com.runescape.drawing.DrawingArea;
import com.runescape.drawing.Texture;
import com.runescape.entity.Renderable;
import com.runescape.io.Stream;
import com.runescape.net.OnDemandFetcherParent;

public class Model extends Renderable {

	public static void nullLoader() {
		modelHeaders = null;
		aBooleanArray1663 = null;
		aBooleanArray1664 = null;
		vertexSY = null;
		anIntArray1667 = null;
		vertexMvX = null;
		vertexMvY = null;
		vertexMvZ = null;
		anIntArray1671 = null;
		anIntArrayArray1672 = null;
		anIntArray1673 = null;
		anIntArrayArray1674 = null;
		anIntArray1675 = null;
		anIntArray1676 = null;
		anIntArray1677 = null;
		modelIntArray1 = null;
		modelIntArray2 = null;
		modelIntArray3 = null;
		modelIntArray4 = null;
	}

	public void read525Model(byte abyte0[], int modelID) {
		Stream nc1 = new Stream(abyte0);
		Stream nc2 = new Stream(abyte0);
		Stream nc3 = new Stream(abyte0);
		Stream nc4 = new Stream(abyte0);
		Stream nc5 = new Stream(abyte0);
		Stream nc6 = new Stream(abyte0);
		Stream nc7 = new Stream(abyte0);
		nc1.currentOffset = abyte0.length - 23;
		int numVertices = nc1.readUnsignedShort();
		int numTriangles = nc1.readUnsignedShort();
		int numTexTriangles = nc1.readUnsignedByte();
		ModelHeader ModelDef_1 = modelHeaders[modelID] = new ModelHeader();
		ModelDef_1.data = abyte0;
		ModelDef_1.vertexCount = numVertices;
		ModelDef_1.triangleCount = numTriangles;
		ModelDef_1.texturedTriangleCount = numTexTriangles;
		int l1 = nc1.readUnsignedByte();
		boolean bool = (0x1 & l1 ^ 0xffffffff) == -2;
		int i2 = nc1.readUnsignedByte();
		int j2 = nc1.readUnsignedByte();
		int k2 = nc1.readUnsignedByte();
		int l2 = nc1.readUnsignedByte();
		int i3 = nc1.readUnsignedByte();
		int j3 = nc1.readUnsignedShort();
		int k3 = nc1.readUnsignedShort();
		int l3 = nc1.readUnsignedShort();
		int i4 = nc1.readUnsignedShort();
		int j4 = nc1.readUnsignedShort();
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		byte[] x = null;
		byte[] O = null;
		byte[] J = null;
		byte[] F = null;
		byte[] cb = null;
		byte[] gb = null;
		byte[] lb = null;
		int[] kb = null;
		int[] y = null;
		int[] N = null;
		short[] D = null;
		int[] triangleColours2 = new int[numTriangles];
		if (numTexTriangles > 0) {
			O = new byte[numTexTriangles];
			nc1.currentOffset = 0;
			for (int j5 = 0; j5 < numTexTriangles; j5++) {
				byte byte0 = O[j5] = nc1.readSignedByte();
				if (byte0 == 0)
					k4++;
				if (byte0 >= 1 && byte0 <= 3)
					l4++;
				if (byte0 == 2)
					i5++;
			}
		}
		int k5 = numTexTriangles;
		int l5 = k5;
		k5 += numVertices;
		int i6 = k5;
		if (l1 == 1)
			k5 += numTriangles;
		int j6 = k5;
		k5 += numTriangles;
		int k6 = k5;
		if (i2 == 255)
			k5 += numTriangles;
		int l6 = k5;
		if (k2 == 1)
			k5 += numTriangles;
		int i7 = k5;
		if (i3 == 1)
			k5 += numVertices;
		int j7 = k5;
		if (j2 == 1)
			k5 += numTriangles;
		int k7 = k5;
		k5 += i4;
		int l7 = k5;
		if (l2 == 1)
			k5 += numTriangles * 2;
		int i8 = k5;
		k5 += j4;
		int j8 = k5;
		k5 += numTriangles * 2;
		int k8 = k5;
		k5 += j3;
		int l8 = k5;
		k5 += k3;
		int i9 = k5;
		k5 += l3;
		int j9 = k5;
		k5 += k4 * 6;
		int k9 = k5;
		k5 += l4 * 6;
		int l9 = k5;
		k5 += l4 * 6;
		int i10 = k5;
		k5 += l4;
		int j10 = k5;
		k5 += l4;
		int k10 = k5;
		k5 += l4 + i5 * 2;
		int[] vertexX = new int[numVertices];
		int[] vertexY = new int[numVertices];
		int[] vertexZ = new int[numVertices];
		int[] facePoint1 = new int[numTriangles];
		int[] facePoint2 = new int[numTriangles];
		int[] facePoint3 = new int[numTriangles];
		anIntArray1655 = new int[numVertices];
		triDrawType = new int[numTriangles];
		anIntArray1638 = new int[numTriangles];
		triangleAlpha = new int[numTriangles];
		anIntArray1656 = new int[numTriangles];
		if (i3 == 1)
			anIntArray1655 = new int[numVertices];
		if (bool)
			triDrawType = new int[numTriangles];
		if (i2 == 255)
			anIntArray1638 = new int[numTriangles];
		else {
		}
		if (j2 == 1)
			triangleAlpha = new int[numTriangles];
		if (k2 == 1)
			anIntArray1656 = new int[numTriangles];
		if (l2 == 1)
			D = new short[numTriangles];
		if (l2 == 1 && numTexTriangles > 0)
			x = new byte[numTriangles];
		triangleColours2 = new int[numTriangles];
		int[] texTrianglesPoint1 = null;
		int[] texTrianglesPoint2 = null;
		int[] texTrianglesPoint3 = null;
		if (numTexTriangles > 0) {
			texTrianglesPoint1 = new int[numTexTriangles];
			texTrianglesPoint2 = new int[numTexTriangles];
			texTrianglesPoint3 = new int[numTexTriangles];
			if (l4 > 0) {
				kb = new int[l4];
				N = new int[l4];
				y = new int[l4];
				gb = new byte[l4];
				lb = new byte[l4];
				F = new byte[l4];
			}
			if (i5 > 0) {
				cb = new byte[i5];
				J = new byte[i5];
			}
		}
		nc1.currentOffset = l5;
		nc2.currentOffset = k8;
		nc3.currentOffset = l8;
		nc4.currentOffset = i9;
		nc5.currentOffset = i7;
		int l10 = 0;
		int i11 = 0;
		int j11 = 0;
		for (int k11 = 0; k11 < numVertices; k11++) {
			int l11 = nc1.readUnsignedByte();
			int j12 = 0;
			if ((l11 & 1) != 0)
				j12 = nc2.method421();
			int l12 = 0;
			if ((l11 & 2) != 0)
				l12 = nc3.method421();
			int j13 = 0;
			if ((l11 & 4) != 0)
				j13 = nc4.method421();
			vertexX[k11] = l10 + j12;
			vertexY[k11] = i11 + l12;
			vertexZ[k11] = j11 + j13;
			l10 = vertexX[k11];
			i11 = vertexY[k11];
			j11 = vertexZ[k11];
			if (anIntArray1655 != null)
				anIntArray1655[k11] = nc5.readUnsignedByte();
		}
		nc1.currentOffset = j8;
		nc2.currentOffset = i6;
		nc3.currentOffset = k6;
		nc4.currentOffset = j7;
		nc5.currentOffset = l6;
		nc6.currentOffset = l7;
		nc7.currentOffset = i8;
		for (int i12 = 0; i12 < numTriangles; i12++) {
			triangleColours2[i12] = nc1.readUnsignedShort();
			if (l1 == 1) {
				triDrawType[i12] = nc2.readSignedByte();
				if (triDrawType[i12] == 2)
					triangleColours2[i12] = 65535;
				triDrawType[i12] = 0;
			}
			if (i2 == 255) {
				anIntArray1638[i12] = nc3.readSignedByte();
			}
			if (j2 == 1) {
				triangleAlpha[i12] = nc4.readSignedByte();
				if (triangleAlpha[i12] < 0)
					triangleAlpha[i12] = (256 + triangleAlpha[i12]);
			}
			if (k2 == 1)
				anIntArray1656[i12] = nc5.readUnsignedByte();
			if (l2 == 1)
				D[i12] = (short) (nc6.readUnsignedShort() - 1);
			if (x != null)
				if (D[i12] != -1)
					x[i12] = (byte) (nc7.readUnsignedByte() - 1);
				else
					x[i12] = -1;
		}
		///fix's triangle issue, but fucked up - no need, loading all 474- models
		/*try {
		for(int i12 = 0; i12 < numTriangles; i12++) {
			triangleColours2[i12] = nc1.readUnsignedWord();
			if(l1 == 1){
				anIntArray1637[i12] = nc2.readSignedByte();
			}
			if(i2 == 255){
				anIntArray1638[i12] = nc3.readSignedByte();
			}
			if(j2 == 1){
				anIntArray1639[i12] = nc4.readSignedByte();
			if(anIntArray1639[i12] < 0)
				anIntArray1639[i12] = (256+anIntArray1639[i12]);
			}
			if(k2 == 1)
				anIntArray1656[i12] = nc5.readUnsignedByte();
			if(l2 == 1)
				D[i12] = (short)(nc6.readUnsignedWord() - 1);
			if(x != null)
				if(D[i12] != -1)
					x[i12] = (byte)(nc7.readUnsignedByte() -1);
			else
				x[i12] = -1;
		}
		} catch (Exception ex) {
		}*/
		nc1.currentOffset = k7;
		nc2.currentOffset = j6;
		int k12 = 0;
		int i13 = 0;
		int k13 = 0;
		int l13 = 0;
		for (int i14 = 0; i14 < numTriangles; i14++) {
			int j14 = nc2.readUnsignedByte();
			if (j14 == 1) {
				k12 = nc1.method421() + l13;
				l13 = k12;
				i13 = nc1.method421() + l13;
				l13 = i13;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
			if (j14 == 2) {
				i13 = k13;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
			if (j14 == 3) {
				k12 = k13;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
			if (j14 == 4) {
				int l14 = k12;
				k12 = i13;
				i13 = l14;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
		}
		nc1.currentOffset = j9;
		nc2.currentOffset = k9;
		nc3.currentOffset = l9;
		nc4.currentOffset = i10;
		nc5.currentOffset = j10;
		nc6.currentOffset = k10;
		for (int k14 = 0; k14 < numTexTriangles; k14++) {
			int i15 = O[k14] & 0xff;
			if (i15 == 0) {
				texTrianglesPoint1[k14] = nc1.readUnsignedShort();
				texTrianglesPoint2[k14] = nc1.readUnsignedShort();
				texTrianglesPoint3[k14] = nc1.readUnsignedShort();
			}
			if (i15 == 1) {
				texTrianglesPoint1[k14] = nc2.readUnsignedShort();
				texTrianglesPoint2[k14] = nc2.readUnsignedShort();
				texTrianglesPoint3[k14] = nc2.readUnsignedShort();
				kb[k14] = nc3.readUnsignedShort();
				N[k14] = nc3.readUnsignedShort();
				y[k14] = nc3.readUnsignedShort();
				gb[k14] = nc4.readSignedByte();
				lb[k14] = nc5.readSignedByte();
				F[k14] = nc6.readSignedByte();
			}
			if (i15 == 2) {
				texTrianglesPoint1[k14] = nc2.readUnsignedShort();
				texTrianglesPoint2[k14] = nc2.readUnsignedShort();
				texTrianglesPoint3[k14] = nc2.readUnsignedShort();
				kb[k14] = nc3.readUnsignedShort();
				N[k14] = nc3.readUnsignedShort();
				y[k14] = nc3.readUnsignedShort();
				gb[k14] = nc4.readSignedByte();
				lb[k14] = nc5.readSignedByte();
				F[k14] = nc6.readSignedByte();
				cb[k14] = nc6.readSignedByte();
				J[k14] = nc6.readSignedByte();
			}
			if (i15 == 3) {
				texTrianglesPoint1[k14] = nc2.readUnsignedShort();
				texTrianglesPoint2[k14] = nc2.readUnsignedShort();
				texTrianglesPoint3[k14] = nc2.readUnsignedShort();
				kb[k14] = nc3.readUnsignedShort();
				N[k14] = nc3.readUnsignedShort();
				y[k14] = nc3.readUnsignedShort();
				gb[k14] = nc4.readSignedByte();
				lb[k14] = nc5.readSignedByte();
				F[k14] = nc6.readSignedByte();
			}
		}
		if (i2 != 255) {
			for (int i12 = 0; i12 < numTriangles; i12++)
				anIntArray1638[i12] = i2;
		}
		triangleColourOrTexture = triangleColours2;
		anInt1626 = numVertices;
		anInt1630 = numTriangles;
		anIntArray1627 = vertexX;
		anIntArray1628 = vertexY;
		anIntArray1629 = vertexZ;
		triangleA = facePoint1;
		triangleB = facePoint2;
		triangleC = facePoint3;
	}

	public Model(int modelId) {
		byte[] is = modelHeaders[modelId].data;
		if (is[is.length - 1] == -1 && is[is.length - 2] == -1)
			read622Model(is, modelId);
		else
			readOldModel(modelId);
		if (isNewModel[modelId] == 1) {
			scale2(4);// 2 is too big -- 3 is almost right
			if(anIntArray1638 != null) {
				for(int j = 0; j < anIntArray1638.length; j++)
					anIntArray1638[j] = 10;
			}
		}
	}

	public void scale2(int i) {
		for (int i1 = 0; i1 < anInt1626; i1++) {
			anIntArray1627[i1] = anIntArray1627[i1] / i;
			anIntArray1628[i1] = anIntArray1628[i1] / i;
			anIntArray1629[i1] = anIntArray1629[i1] / i;
		}
	}

	public void read622Model(byte abyte0[], int modelID) {
		Stream nc1 = new Stream(abyte0);
		Stream nc2 = new Stream(abyte0);
		Stream nc3 = new Stream(abyte0);
		Stream nc4 = new Stream(abyte0);
		Stream nc5 = new Stream(abyte0);
		Stream nc6 = new Stream(abyte0);
		Stream nc7 = new Stream(abyte0);
		nc1.currentOffset = abyte0.length - 23;
		int numVertices = nc1.readUnsignedShort();
		int numTriangles = nc1.readUnsignedShort();
		int numTexTriangles = nc1.readUnsignedByte();
		ModelHeader ModelDef_1 = modelHeaders[modelID] = new ModelHeader();
		ModelDef_1.data = abyte0;
		ModelDef_1.vertexCount = numVertices;
		ModelDef_1.triangleCount = numTriangles;
		ModelDef_1.texturedTriangleCount = numTexTriangles;
		int l1 = nc1.readUnsignedByte();
		boolean bool = (0x1 & l1 ^ 0xffffffff) == -2;
		boolean bool_26_ = (0x8 & l1) == 8;
		if (!bool_26_) {
			read525Model(abyte0, modelID);
			return;
		}
		int newformat = 0;
		if (bool_26_) {
			nc1.currentOffset -= 7;
			newformat = nc1.readUnsignedByte();
			nc1.currentOffset += 6;
		}
		if (newformat == 15) {
			isNewModel[modelID] = 1;
		}
		int i2 = nc1.readUnsignedByte();
		int j2 = nc1.readUnsignedByte();
		int k2 = nc1.readUnsignedByte();
		int l2 = nc1.readUnsignedByte();
		int i3 = nc1.readUnsignedByte();
		int j3 = nc1.readUnsignedShort();
		int k3 = nc1.readUnsignedShort();
		int l3 = nc1.readUnsignedShort();
		int i4 = nc1.readUnsignedShort();
		int j4 = nc1.readUnsignedShort();
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		byte[] x = null;
		byte[] O = null;
		byte[] J = null;
		byte[] F = null;
		byte[] cb = null;
		byte[] gb = null;
		byte[] lb = null;
		int[] kb = null;
		int[] y = null;
		int[] N = null;
		short[] D = null;
		int[] triangleColours2 = new int[numTriangles];
		if (numTexTriangles > 0) {
			O = new byte[numTexTriangles];
			nc1.currentOffset = 0;
			for (int j5 = 0; j5 < numTexTriangles; j5++) {
				byte byte0 = O[j5] = nc1.readSignedByte();
				if (byte0 == 0)
					k4++;
				if (byte0 >= 1 && byte0 <= 3)
					l4++;
				if (byte0 == 2)
					i5++;
			}
		}
		int k5 = numTexTriangles;
		int l5 = k5;
		k5 += numVertices;
		int i6 = k5;
		if (bool)
			k5 += numTriangles;
		if (l1 == 1)
			k5 += numTriangles;
		int j6 = k5;
		k5 += numTriangles;
		int k6 = k5;
		if (i2 == 255)
			k5 += numTriangles;
		int l6 = k5;
		if (k2 == 1)
			k5 += numTriangles;
		int i7 = k5;
		if (i3 == 1)
			k5 += numVertices;
		int j7 = k5;
		if (j2 == 1)
			k5 += numTriangles;
		int k7 = k5;
		k5 += i4;
		int l7 = k5;
		if (l2 == 1)
			k5 += numTriangles * 2;
		int i8 = k5;
		k5 += j4;
		int j8 = k5;
		k5 += numTriangles * 2;
		int k8 = k5;
		k5 += j3;
		int l8 = k5;
		k5 += k3;
		int i9 = k5;
		k5 += l3;
		int j9 = k5;
		k5 += k4 * 6;
		int k9 = k5;
		k5 += l4 * 6;
		int i_59_ = 6;
		if (newformat != 14) {
			if (newformat >= 15)
				i_59_ = 9;
		} else
			i_59_ = 7;
		int l9 = k5;
		k5 += i_59_ * l4;
		int i10 = k5;
		k5 += l4;
		int j10 = k5;
		k5 += l4;
		int k10 = k5;
		k5 += l4 + i5 * 2;
		int[] vertexX = new int[numVertices];
		int[] vertexY = new int[numVertices];
		int[] vertexZ = new int[numVertices];
		int[] facePoint1 = new int[numTriangles];
		int[] facePoint2 = new int[numTriangles];
		int[] facePoint3 = new int[numTriangles];
		anIntArray1655 = new int[numVertices];
		triDrawType = new int[numTriangles];
		anIntArray1638 = new int[numTriangles];
		triangleAlpha = new int[numTriangles];
		anIntArray1656 = new int[numTriangles];
		if (i3 == 1)
			anIntArray1655 = new int[numVertices];
		if (bool)
			triDrawType = new int[numTriangles];
		if (i2 == 255)
			anIntArray1638 = new int[numTriangles];
		else {
		}
		if (j2 == 1)
			triangleAlpha = new int[numTriangles];
		if (k2 == 1)
			anIntArray1656 = new int[numTriangles];
		if (l2 == 1)
			D = new short[numTriangles];
		if (l2 == 1 && numTexTriangles > 0)
			x = new byte[numTriangles];
		triangleColours2 = new int[numTriangles];
		int[] texTrianglesPoint1 = null;
		int[] texTrianglesPoint2 = null;
		int[] texTrianglesPoint3 = null;
		if (numTexTriangles > 0) {
			texTrianglesPoint1 = new int[numTexTriangles];
			texTrianglesPoint2 = new int[numTexTriangles];
			texTrianglesPoint3 = new int[numTexTriangles];
			if (l4 > 0) {
				kb = new int[l4];
				N = new int[l4];
				y = new int[l4];
				gb = new byte[l4];
				lb = new byte[l4];
				F = new byte[l4];
			}
			if (i5 > 0) {
				cb = new byte[i5];
				J = new byte[i5];
			}
		}
		nc1.currentOffset = l5;
		nc2.currentOffset = k8;
		nc3.currentOffset = l8;
		nc4.currentOffset = i9;
		nc5.currentOffset = i7;
		int l10 = 0;
		int i11 = 0;
		int j11 = 0;
		for (int k11 = 0; k11 < numVertices; k11++) {
			int l11 = nc1.readUnsignedByte();
			int j12 = 0;
			if ((l11 & 1) != 0)
				j12 = nc2.method421();
			int l12 = 0;
			if ((l11 & 2) != 0)
				l12 = nc3.method421();
			int j13 = 0;
			if ((l11 & 4) != 0)
				j13 = nc4.method421();
			vertexX[k11] = l10 + j12;
			vertexY[k11] = i11 + l12;
			vertexZ[k11] = j11 + j13;
			l10 = vertexX[k11];
			i11 = vertexY[k11];
			j11 = vertexZ[k11];
			if (anIntArray1655 != null)
				anIntArray1655[k11] = nc5.readUnsignedByte();
		}
		nc1.currentOffset = j8;
		nc2.currentOffset = i6;
		nc3.currentOffset = k6;
		nc4.currentOffset = j7;
		nc5.currentOffset = l6;
		nc6.currentOffset = l7;
		nc7.currentOffset = i8;
		for (int i12 = 0; i12 < numTriangles; i12++) {
			triangleColours2[i12] = nc1.readUnsignedShort();
			if (l1 == 1) {
				triDrawType[i12] = nc2.readSignedByte();
				if (triDrawType[i12] == 2)
					triangleColours2[i12] = 65535;
				triDrawType[i12] = 0;
			}
			if (i2 == 255) {
				anIntArray1638[i12] = nc3.readSignedByte();
			}
			if (j2 == 1) {
				triangleAlpha[i12] = nc4.readSignedByte();
				if (triangleAlpha[i12] < 0)
					triangleAlpha[i12] = (256 + triangleAlpha[i12]);
			}
			if (k2 == 1)
				anIntArray1656[i12] = nc5.readUnsignedByte();
			if (l2 == 1)
				D[i12] = (short) (nc6.readUnsignedShort() - 1);
			if (x != null)
				if (D[i12] != -1)
					x[i12] = (byte) (nc7.readUnsignedByte() - 1);
				else
					x[i12] = -1;
		}
		nc1.currentOffset = k7;
		nc2.currentOffset = j6;
		int k12 = 0;
		int i13 = 0;
		int k13 = 0;
		int l13 = 0;
		for (int i14 = 0; i14 < numTriangles; i14++) {
			int j14 = nc2.readUnsignedByte();
			if (j14 == 1) {
				k12 = nc1.method421() + l13;
				l13 = k12;
				i13 = nc1.method421() + l13;
				l13 = i13;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
			if (j14 == 2) {
				i13 = k13;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
			if (j14 == 3) {
				k12 = k13;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
			if (j14 == 4) {
				int l14 = k12;
				k12 = i13;
				i13 = l14;
				k13 = nc1.method421() + l13;
				l13 = k13;
				facePoint1[i14] = k12;
				facePoint2[i14] = i13;
				facePoint3[i14] = k13;
			}
		}
		nc1.currentOffset = j9;
		nc2.currentOffset = k9;
		nc3.currentOffset = l9;
		nc4.currentOffset = i10;
		nc5.currentOffset = j10;
		nc6.currentOffset = k10;
		for (int k14 = 0; k14 < numTexTriangles; k14++) {
			int i15 = O[k14] & 0xff;
			if (i15 == 0) {
				texTrianglesPoint1[k14] = nc1.readUnsignedShort();
				texTrianglesPoint2[k14] = nc1.readUnsignedShort();
				texTrianglesPoint3[k14] = nc1.readUnsignedShort();
			}
			if (i15 == 1) {
				texTrianglesPoint1[k14] = nc2.readUnsignedShort();
				texTrianglesPoint2[k14] = nc2.readUnsignedShort();
				texTrianglesPoint3[k14] = nc2.readUnsignedShort();
				if (newformat < 15) {
					kb[k14] = nc3.readUnsignedShort();
					if (newformat >= 14)
						N[k14] = nc3.v(-1);
					else
						N[k14] = nc3.readUnsignedShort();
					y[k14] = nc3.readUnsignedShort();
				} else {
					kb[k14] = nc3.v(-1);
					N[k14] = nc3.v(-1);
					y[k14] = nc3.v(-1);
				}
				gb[k14] = nc4.readSignedByte();
				lb[k14] = nc5.readSignedByte();
				F[k14] = nc6.readSignedByte();
			}
			if (i15 == 2) {
				texTrianglesPoint1[k14] = nc2.readUnsignedShort();
				texTrianglesPoint2[k14] = nc2.readUnsignedShort();
				texTrianglesPoint3[k14] = nc2.readUnsignedShort();
				if (newformat >= 15) {
					kb[k14] = nc3.v(-1);
					N[k14] = nc3.v(-1);
					y[k14] = nc3.v(-1);
				} else {
					kb[k14] = nc3.readUnsignedShort();
					if (newformat < 14)
						N[k14] = nc3.readUnsignedShort();
					else
						N[k14] = nc3.v(-1);
					y[k14] = nc3.readUnsignedShort();
				}
				gb[k14] = nc4.readSignedByte();
				lb[k14] = nc5.readSignedByte();
				F[k14] = nc6.readSignedByte();
				cb[k14] = nc6.readSignedByte();
				J[k14] = nc6.readSignedByte();
			}
			if (i15 == 3) {
				texTrianglesPoint1[k14] = nc2.readUnsignedShort();
				texTrianglesPoint2[k14] = nc2.readUnsignedShort();
				texTrianglesPoint3[k14] = nc2.readUnsignedShort();
				if (newformat < 15) {
					kb[k14] = nc3.readUnsignedShort();
					if (newformat < 14)
						N[k14] = nc3.readUnsignedShort();
					else
						N[k14] = nc3.v(-1);
					y[k14] = nc3.readUnsignedShort();
				} else {
					kb[k14] = nc3.v(-1);
					N[k14] = nc3.v(-1);
					y[k14] = nc3.v(-1);
				}
				gb[k14] = nc4.readSignedByte();
				lb[k14] = nc5.readSignedByte();
				F[k14] = nc6.readSignedByte();
			}
		}
		if (i2 != 255) {
			for (int i12 = 0; i12 < numTriangles; i12++)
				anIntArray1638[i12] = i2;
		}
		triangleColourOrTexture = triangleColours2;
		anInt1626 = numVertices;
		anInt1630 = numTriangles;
		anIntArray1627 = vertexX;
		anIntArray1628 = vertexY;
		anIntArray1629 = vertexZ;
		triangleA = facePoint1;
		triangleB = facePoint2;
		triangleC = facePoint3;
	}

	private void readOldModel(int i) {
		int j = -870;
		aBoolean1618 = true;
		aBoolean1659 = false;
		anInt1620++;
		ModelHeader class21 = modelHeaders[i];
		anInt1626 = class21.vertexCount;
		anInt1630 = class21.triangleCount;
		anInt1642 = class21.texturedTriangleCount;
		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		triangleA = new int[anInt1630];
		triangleB = new int[anInt1630];
		while (j >= 0)
			aBoolean1618 = !aBoolean1618;
		triangleC = new int[anInt1630];
		triPIndex = new int[anInt1642];
		triMIndex = new int[anInt1642];
		triNIndex = new int[anInt1642];
		if (class21.vertexSkinOffset >= 0)
			anIntArray1655 = new int[anInt1626];
		if (class21.texturePointerOffset >= 0)
			triDrawType = new int[anInt1630];
		if (class21.trianglePriorityOffset >= 0)
			anIntArray1638 = new int[anInt1630];
		else
			anInt1641 = -class21.trianglePriorityOffset - 1;
		if (class21.triangleAlphaOffset >= 0)
			triangleAlpha = new int[anInt1630];
		if (class21.triangleSkinOffset >= 0)
			anIntArray1656 = new int[anInt1630];
		triangleColourOrTexture = new int[anInt1630];
		Stream stream = new Stream(class21.data);
		stream.currentOffset = class21.vertexDirectionOffset;
		Stream stream_1 = new Stream(class21.data);
		stream_1.currentOffset = class21.xDataOffset;
		Stream stream_2 = new Stream(class21.data);
		stream_2.currentOffset = class21.yDataOffset;
		Stream stream_3 = new Stream(class21.data);
		stream_3.currentOffset = class21.zDataOffset;
		Stream stream_4 = new Stream(class21.data);
		stream_4.currentOffset = class21.vertexSkinOffset;
		int k = 0;
		int l = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < anInt1626; j1++) {
			int k1 = stream.readUnsignedByte();
			int i2 = 0;
			if ((k1 & 1) != 0)
				i2 = stream_1.method421();
			int k2 = 0;
			if ((k1 & 2) != 0)
				k2 = stream_2.method421();
			int i3 = 0;
			if ((k1 & 4) != 0)
				i3 = stream_3.method421();
			anIntArray1627[j1] = k + i2;
			anIntArray1628[j1] = l + k2;
			anIntArray1629[j1] = i1 + i3;
			k = anIntArray1627[j1];
			l = anIntArray1628[j1];
			i1 = anIntArray1629[j1];
			if (anIntArray1655 != null)
				anIntArray1655[j1] = stream_4.readUnsignedByte();
		}
		stream.currentOffset = class21.colorDataOffset;
		stream_1.currentOffset = class21.texturePointerOffset;
		stream_2.currentOffset = class21.trianglePriorityOffset;
		stream_3.currentOffset = class21.triangleAlphaOffset;
		stream_4.currentOffset = class21.triangleSkinOffset;
		for (int l1 = 0; l1 < anInt1630; l1++) {
			triangleColourOrTexture[l1] = stream.readUnsignedShort();
			if (triDrawType != null)
				triDrawType[l1] = stream_1.readUnsignedByte();
			if (anIntArray1638 != null)
				anIntArray1638[l1] = stream_2.readUnsignedByte();
			if (triangleAlpha != null) {
				triangleAlpha[l1] = stream_3.readUnsignedByte();
			}
			if (anIntArray1656 != null)
				anIntArray1656[l1] = stream_4.readUnsignedByte();
		}
		stream.currentOffset = class21.triangleDataOffset;
		stream_1.currentOffset = class21.triangleTypeOffset;
		int j2 = 0;
		int l2 = 0;
		int j3 = 0;
		int k3 = 0;
		for (int l3 = 0; l3 < anInt1630; l3++) {
			int i4 = stream_1.readUnsignedByte();
			if (i4 == 1) {
				j2 = stream.method421() + k3;
				k3 = j2;
				l2 = stream.method421() + k3;
				k3 = l2;
				j3 = stream.method421() + k3;
				k3 = j3;
				triangleA[l3] = j2;
				triangleB[l3] = l2;
				triangleC[l3] = j3;
			}
			if (i4 == 2) {
				l2 = j3;
				j3 = stream.method421() + k3;
				k3 = j3;
				triangleA[l3] = j2;
				triangleB[l3] = l2;
				triangleC[l3] = j3;
			}
			if (i4 == 3) {
				j2 = j3;
				j3 = stream.method421() + k3;
				k3 = j3;
				triangleA[l3] = j2;
				triangleB[l3] = l2;
				triangleC[l3] = j3;
			}
			if (i4 == 4) {
				int k4 = j2;
				j2 = l2;
				l2 = k4;
				j3 = stream.method421() + k3;
				k3 = j3;
				triangleA[l3] = j2;
				triangleB[l3] = l2;
				triangleC[l3] = j3;
			}
		}
		stream.currentOffset = class21.uvMapTriangleOffset;
		for (int j4 = 0; j4 < anInt1642; j4++) {
			triPIndex[j4] = stream.readUnsignedShort();
			triMIndex[j4] = stream.readUnsignedShort();
			triNIndex[j4] = stream.readUnsignedShort();
		}
	}
	
	public static void init(int modelCount, OnDemandFetcherParent onDemandFetcherParent) {
		modelHeaders = new ModelHeader[modelCount];
		isNewModel = new byte[modelCount];
		modelOnDemandFetcher = onDemandFetcherParent;
	}

	public static void loadModelHeader(byte[] modelData, int modelId) {
		try {
			if (modelData == null) {
				ModelHeader modelheader = modelHeaders[modelId] = new ModelHeader();
				modelheader.vertexCount = 0;
				modelheader.triangleCount = 0;
				modelheader.texturedTriangleCount = 0;
				return;
			}
			Stream stream = new Stream(modelData);
			stream.currentOffset = modelData.length - 18;
			ModelHeader modelheader = modelHeaders[modelId] = new ModelHeader();
			modelheader.data = modelData;
			modelheader.vertexCount = stream.readUnsignedShort();
			modelheader.triangleCount = stream.readUnsignedShort();
			modelheader.texturedTriangleCount = stream.readUnsignedByte();
			int useTextures = stream.readUnsignedByte();
			int useTrianglePriority = stream.readUnsignedByte();
			int useTransparency = stream.readUnsignedByte();
			int useTriangleSkinning = stream.readUnsignedByte();
			int useVertexSkinning = stream.readUnsignedByte();
			int xDataLength = stream.readUnsignedShort();
			int yDataLength = stream.readUnsignedShort();
			int zDataLength = stream.readUnsignedShort();
			int triangleDataLength = stream.readUnsignedShort();
			int offset = 0;
			modelheader.vertexDirectionOffset = offset;
			offset += modelheader.vertexCount;
			modelheader.triangleTypeOffset = offset;
			offset += modelheader.triangleCount;
			modelheader.trianglePriorityOffset = offset;
			if (useTrianglePriority == 255)
				offset += modelheader.triangleCount;
			else
				modelheader.trianglePriorityOffset = -useTrianglePriority - 1;
			modelheader.triangleSkinOffset = offset;
			if (useTriangleSkinning == 1)
				offset += modelheader.triangleCount;
			else
				modelheader.triangleSkinOffset = -1;
			modelheader.texturePointerOffset = offset;
			if (useTextures == 1)
				offset += modelheader.triangleCount;
			else
				modelheader.texturePointerOffset = -1;
			modelheader.vertexSkinOffset = offset;
			if (useVertexSkinning == 1)
				offset += modelheader.vertexCount;
			else
				modelheader.vertexSkinOffset = -1;
			modelheader.triangleAlphaOffset = offset;
			if (useTransparency == 1)
				offset += modelheader.triangleCount;
			else
				modelheader.triangleAlphaOffset = -1;
			modelheader.triangleDataOffset = offset;
			offset += triangleDataLength;
			modelheader.colorDataOffset = offset;
			offset += modelheader.triangleCount * 2;
			modelheader.uvMapTriangleOffset = offset;
			offset += modelheader.texturedTriangleCount * 6;
			modelheader.xDataOffset = offset;
			offset += xDataLength;
			modelheader.yDataOffset = offset;
			offset += yDataLength;
			modelheader.zDataOffset = offset;
			offset += zDataLength;
		} catch (Exception _ex) { }
	}

	public static void resetModel(int j) {
		modelHeaders[j] = null;
	}

	public static Model getModel(int j) {
		if (modelHeaders == null || j == -1 || j >= modelHeaders.length)
			return null;
		ModelHeader modelHeader = modelHeaders[j];
		if (modelHeader == null) {
			modelOnDemandFetcher.request(j);
			return null;
		} else {
			return new Model(j);
		}
	}

	public static boolean isCached(int i) {
		if (modelHeaders == null)
			return false;

		ModelHeader class21 = modelHeaders[i];
		if (class21 == null) {
			modelOnDemandFetcher.request(i);
			return false;
		} else {
			return true;
		}
	}

	private Model(boolean flag) {
		aBoolean1618 = true;
		aBoolean1659 = false;
		if (!flag)
			aBoolean1618 = !aBoolean1618;
	}

	public Model(int i, Model amodel[]) {
		aBoolean1618 = true;
		aBoolean1659 = false;
		anInt1620++;
		boolean flag = false;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		anInt1641 = -1;
		for (int k = 0; k < i; k++) {
			Model model = amodel[k];
			if (model != null) {
				anInt1626 += model.anInt1626;
				anInt1630 += model.anInt1630;
				anInt1642 += model.anInt1642;
				flag |= model.triDrawType != null;
				if (model.anIntArray1638 != null) {
					flag1 = true;
				} else {
					if (anInt1641 == -1)
						anInt1641 = model.anInt1641;
					if (anInt1641 != model.anInt1641)
						flag1 = true;
				}
				flag2 |= model.triangleAlpha != null;
				flag3 |= model.anIntArray1656 != null;
			}
		}

		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		anIntArray1655 = new int[anInt1626];
		triangleA = new int[anInt1630];
		triangleB = new int[anInt1630];
		triangleC = new int[anInt1630];
		triPIndex = new int[anInt1642];
		triMIndex = new int[anInt1642];
		triNIndex = new int[anInt1642];
		if (flag)
			triDrawType = new int[anInt1630];
		if (flag1)
			anIntArray1638 = new int[anInt1630];
		if (flag2)
			triangleAlpha = new int[anInt1630];
		if (flag3)
			anIntArray1656 = new int[anInt1630];
		triangleColourOrTexture = new int[anInt1630];
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		int l = 0;
		for (int i1 = 0; i1 < i; i1++) {
			Model model_1 = amodel[i1];
			if (model_1 != null) {
				for (int j1 = 0; j1 < model_1.anInt1630; j1++) {
					if (flag)
						if (model_1.triDrawType == null) {
							triDrawType[anInt1630] = 0;
						} else {
							int k1 = model_1.triDrawType[j1];
							if ((k1 & 2) == 2)
								k1 += l << 2;
							triDrawType[anInt1630] = k1;
						}
					if (flag1)
						if (model_1.anIntArray1638 == null)
							anIntArray1638[anInt1630] = model_1.anInt1641;
						else
							anIntArray1638[anInt1630] = model_1.anIntArray1638[j1];
					if (flag2)
						if (model_1.triangleAlpha == null)
							triangleAlpha[anInt1630] = 0;
						else
							triangleAlpha[anInt1630] = model_1.triangleAlpha[j1];

					if (flag3 && model_1.anIntArray1656 != null)
						anIntArray1656[anInt1630] = model_1.anIntArray1656[j1];
					triangleColourOrTexture[anInt1630] = model_1.triangleColourOrTexture[j1];
					triangleA[anInt1630] = method465(model_1,
							model_1.triangleA[j1]);
					triangleB[anInt1630] = method465(model_1,
							model_1.triangleB[j1]);
					triangleC[anInt1630] = method465(model_1,
							model_1.triangleC[j1]);
					anInt1630++;
				}

				for (int l1 = 0; l1 < model_1.anInt1642; l1++) {
					triPIndex[anInt1642] = method465(model_1,
							model_1.triPIndex[l1]);
					triMIndex[anInt1642] = method465(model_1,
							model_1.triMIndex[l1]);
					triNIndex[anInt1642] = method465(model_1,
							model_1.triNIndex[l1]);
					anInt1642++;
				}

				l += model_1.anInt1642;
			}
		}

	}

	public Model(Model amodel[]) {
		int i = 2;
		aBoolean1618 = true;
		aBoolean1659 = false;
		anInt1620++;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		boolean flag4 = false;
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		anInt1641 = -1;
		for (int k = 0; k < i; k++) {
			Model model = amodel[k];
			if (model != null) {
				anInt1626 += model.anInt1626;
				anInt1630 += model.anInt1630;
				anInt1642 += model.anInt1642;
				flag1 |= model.triDrawType != null;
				if (model.anIntArray1638 != null) {
					flag2 = true;
				} else {
					if (anInt1641 == -1)
						anInt1641 = model.anInt1641;
					if (anInt1641 != model.anInt1641)
						flag2 = true;
				}
				flag3 |= model.triangleAlpha != null;
				flag4 |= model.triangleColourOrTexture != null;
			}
		}

		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		triangleA = new int[anInt1630];
		triangleB = new int[anInt1630];
		triangleC = new int[anInt1630];
		triangleHslA = new int[anInt1630];
		triangleHslB = new int[anInt1630];
		triangleHslC = new int[anInt1630];
		triPIndex = new int[anInt1642];
		triMIndex = new int[anInt1642];
		triNIndex = new int[anInt1642];
		if (flag1)
			triDrawType = new int[anInt1630];
		if (flag2)
			anIntArray1638 = new int[anInt1630];
		if (flag3)
			triangleAlpha = new int[anInt1630];
		if (flag4)
			triangleColourOrTexture = new int[anInt1630];
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < i; j1++) {
			Model model_1 = amodel[j1];
			if (model_1 != null) {
				int k1 = anInt1626;
				for (int l1 = 0; l1 < model_1.anInt1626; l1++) {
					anIntArray1627[anInt1626] = model_1.anIntArray1627[l1];
					anIntArray1628[anInt1626] = model_1.anIntArray1628[l1];
					anIntArray1629[anInt1626] = model_1.anIntArray1629[l1];
					anInt1626++;
				}

				for (int i2 = 0; i2 < model_1.anInt1630; i2++) {
					triangleA[anInt1630] = model_1.triangleA[i2] + k1;
					triangleB[anInt1630] = model_1.triangleB[i2] + k1;
					triangleC[anInt1630] = model_1.triangleC[i2] + k1;
					triangleHslA[anInt1630] = model_1.triangleHslA[i2];
					triangleHslB[anInt1630] = model_1.triangleHslB[i2];
					triangleHslC[anInt1630] = model_1.triangleHslC[i2];
					if (flag1)
						if (model_1.triDrawType == null) {
							triDrawType[anInt1630] = 0;
						} else {
							int j2 = model_1.triDrawType[i2];
							if ((j2 & 2) == 2)
								j2 += i1 << 2;
							triDrawType[anInt1630] = j2;
						}
					if (flag2)
						if (model_1.anIntArray1638 == null)
							anIntArray1638[anInt1630] = model_1.anInt1641;
						else
							anIntArray1638[anInt1630] = model_1.anIntArray1638[i2];
					if (flag3)
						if (model_1.triangleAlpha == null)
							triangleAlpha[anInt1630] = 0;
						else
							triangleAlpha[anInt1630] = model_1.triangleAlpha[i2];
					if (flag4 && model_1.triangleColourOrTexture != null)
						triangleColourOrTexture[anInt1630] = model_1.triangleColourOrTexture[i2];

					anInt1630++;
				}

				for (int k2 = 0; k2 < model_1.anInt1642; k2++) {
					triPIndex[anInt1642] = model_1.triPIndex[k2] + k1;
					triMIndex[anInt1642] = model_1.triMIndex[k2] + k1;
					triNIndex[anInt1642] = model_1.triNIndex[k2] + k1;
					anInt1642++;
				}

				i1 += model_1.anInt1642;
			}
		}

		method466();
	}

	public Model(boolean flag, boolean flag1, boolean flag2, Model model) {
		aBoolean1618 = true;
		aBoolean1659 = false;
		anInt1620++;
		anInt1626 = model.anInt1626;
		anInt1630 = model.anInt1630;
		anInt1642 = model.anInt1642;
		if (flag2) {
			anIntArray1627 = model.anIntArray1627;
			anIntArray1628 = model.anIntArray1628;
			anIntArray1629 = model.anIntArray1629;
		} else {
			anIntArray1627 = new int[anInt1626];
			anIntArray1628 = new int[anInt1626];
			anIntArray1629 = new int[anInt1626];
			for (int j = 0; j < anInt1626; j++) {
				anIntArray1627[j] = model.anIntArray1627[j];
				anIntArray1628[j] = model.anIntArray1628[j];
				anIntArray1629[j] = model.anIntArray1629[j];
			}

		}
		if (flag) {
			triangleColourOrTexture = model.triangleColourOrTexture;
		} else {
			triangleColourOrTexture = new int[anInt1630];
			for (int k = 0; k < anInt1630; k++)
				triangleColourOrTexture[k] = model.triangleColourOrTexture[k];

		}
		if (flag1) {
			triangleAlpha = model.triangleAlpha;
		} else {
			triangleAlpha = new int[anInt1630];
			if (model.triangleAlpha == null) {
				for (int l = 0; l < anInt1630; l++)
					triangleAlpha[l] = 0;

			} else {
				for (int i1 = 0; i1 < anInt1630; i1++)
					triangleAlpha[i1] = model.triangleAlpha[i1];

			}
		}
		anIntArray1655 = model.anIntArray1655;
		anIntArray1656 = model.anIntArray1656;
		triDrawType = model.triDrawType;
		triangleA = model.triangleA;
		triangleB = model.triangleB;
		triangleC = model.triangleC;
		anIntArray1638 = model.anIntArray1638;
		anInt1641 = model.anInt1641;
		triPIndex = model.triPIndex;
		triMIndex = model.triMIndex;
		triNIndex = model.triNIndex;
	}

	public Model(boolean flag, boolean flag1, Model model) {
		aBoolean1618 = true;
		aBoolean1659 = false;
		anInt1620++;
		anInt1626 = model.anInt1626;
		anInt1630 = model.anInt1630;
		anInt1642 = model.anInt1642;
		if (flag) {
			anIntArray1628 = new int[anInt1626];
			for (int j = 0; j < anInt1626; j++)
				anIntArray1628[j] = model.anIntArray1628[j];

		} else {
			anIntArray1628 = model.anIntArray1628;
		}
		if (flag1) {
			triangleHslA = new int[anInt1630];
			triangleHslB = new int[anInt1630];
			triangleHslC = new int[anInt1630];
			for (int k = 0; k < anInt1630; k++) {
				triangleHslA[k] = model.triangleHslA[k];
				triangleHslB[k] = model.triangleHslB[k];
				triangleHslC[k] = model.triangleHslC[k];
			}

			triDrawType = new int[anInt1630];
			if (model.triDrawType == null) {
				for (int l = 0; l < anInt1630; l++)
					triDrawType[l] = 0;

			} else {
				for (int i1 = 0; i1 < anInt1630; i1++)
					triDrawType[i1] = model.triDrawType[i1];

			}
			super.normals = new VertexNormal[anInt1626];
			for (int j1 = 0; j1 < anInt1626; j1++) {
				VertexNormal class33 = super.normals[j1] = new VertexNormal();
				VertexNormal class33_1 = model.normals[j1];
				class33.anInt602 = class33_1.anInt602;
				class33.anInt603 = class33_1.anInt603;
				class33.anInt604 = class33_1.anInt604;
				class33.anInt605 = class33_1.anInt605;
			}

			aClass33Array1660 = model.aClass33Array1660;
		} else {
			triangleHslA = model.triangleHslA;
			triangleHslB = model.triangleHslB;
			triangleHslC = model.triangleHslC;
			triDrawType = model.triDrawType;
		}
		anIntArray1627 = model.anIntArray1627;
		anIntArray1629 = model.anIntArray1629;
		triangleColourOrTexture = model.triangleColourOrTexture;
		triangleAlpha = model.triangleAlpha;
		anIntArray1638 = model.anIntArray1638;
		anInt1641 = model.anInt1641;
		triangleA = model.triangleA;
		triangleB = model.triangleB;
		triangleC = model.triangleC;
		triPIndex = model.triPIndex;
		triMIndex = model.triMIndex;
		triNIndex = model.triNIndex;
		super.modelHeight = model.modelHeight;

		anInt1650 = model.anInt1650;
		anInt1653 = model.anInt1653;
		anInt1652 = model.anInt1652;
		anInt1646 = model.anInt1646;
		anInt1648 = model.anInt1648;
		anInt1649 = model.anInt1649;
		anInt1647 = model.anInt1647;
	}

	public void method464(Model model, boolean flag) {
		anInt1626 = model.anInt1626;
		anInt1630 = model.anInt1630;
		anInt1642 = model.anInt1642;
		if (anIntArray1622.length < anInt1626) {
			anIntArray1622 = new int[anInt1626 + 10000];
			anIntArray1623 = new int[anInt1626 + 10000];
			anIntArray1624 = new int[anInt1626 + 10000];
		}
		anIntArray1627 = anIntArray1622;
		anIntArray1628 = anIntArray1623;
		anIntArray1629 = anIntArray1624;
		for (int k = 0; k < anInt1626; k++) {
			anIntArray1627[k] = model.anIntArray1627[k];
			anIntArray1628[k] = model.anIntArray1628[k];
			anIntArray1629[k] = model.anIntArray1629[k];
		}

		if (flag) {
			triangleAlpha = model.triangleAlpha;
		} else {
			if (anIntArray1625.length < anInt1630)
				anIntArray1625 = new int[anInt1630 + 100];
			triangleAlpha = anIntArray1625;
			if (model.triangleAlpha == null) {
				for (int l = 0; l < anInt1630; l++)
					triangleAlpha[l] = 0;

			} else {
				for (int i1 = 0; i1 < anInt1630; i1++)
					triangleAlpha[i1] = model.triangleAlpha[i1];

			}
		}
		triDrawType = model.triDrawType;
		triangleColourOrTexture = model.triangleColourOrTexture;
		anIntArray1638 = model.anIntArray1638;
		anInt1641 = model.anInt1641;
		anIntArrayArray1658 = model.anIntArrayArray1658;
		anIntArrayArray1657 = model.anIntArrayArray1657;
		triangleA = model.triangleA;
		triangleB = model.triangleB;
		triangleC = model.triangleC;
		triangleHslA = model.triangleHslA;
		triangleHslB = model.triangleHslB;
		triangleHslC = model.triangleHslC;
		triPIndex = model.triPIndex;
		triMIndex = model.triMIndex;
		triNIndex = model.triNIndex;
	}

	private final int method465(Model model, int i) {
		int j = -1;
		int k = model.anIntArray1627[i];
		int l = model.anIntArray1628[i];
		int i1 = model.anIntArray1629[i];
		for (int j1 = 0; j1 < anInt1626; j1++) {
			if (k != anIntArray1627[j1] || l != anIntArray1628[j1]
			                                                   || i1 != anIntArray1629[j1])
				continue;
			j = j1;
			break;
		}

		if (j == -1) {
			anIntArray1627[anInt1626] = k;
			anIntArray1628[anInt1626] = l;
			anIntArray1629[anInt1626] = i1;
			if (model.anIntArray1655 != null)
				anIntArray1655[anInt1626] = model.anIntArray1655[i];
			j = anInt1626++;
		}
		return j;
	}

	public void method466() {
		super.modelHeight = 0;
		anInt1650 = 0;
		anInt1651 = 0;
		for (int i = 0; i < anInt1626; i++) {
			int j = anIntArray1627[i];
			int k = anIntArray1628[i];
			int l = anIntArray1629[i];
			if (-k > super.modelHeight)
				super.modelHeight = -k;
			if (k > anInt1651)
				anInt1651 = k;
			int i1 = j * j + l * l;
			if (i1 > anInt1650)
				anInt1650 = i1;
		}
		anInt1650 = (int) (Math.sqrt(anInt1650) + 0.98999999999999999D);
		anInt1653 = (int) (Math.sqrt(anInt1650 * anInt1650 + super.modelHeight
				* super.modelHeight) + 0.98999999999999999D);
		anInt1652 = anInt1653
		+ (int) (Math.sqrt(anInt1650 * anInt1650 + anInt1651
				* anInt1651) + 0.98999999999999999D);
	}

	public void method467() {
		super.modelHeight = 0;
		anInt1651 = 0;
		for (int i = 0; i < anInt1626; i++) {
			int j = anIntArray1628[i];
			if (-j > super.modelHeight)
				super.modelHeight = -j;
			if (j > anInt1651)
				anInt1651 = j;
		}

		anInt1653 = (int) (Math.sqrt(anInt1650 * anInt1650 + super.modelHeight
				* super.modelHeight) + 0.98999999999999999D);
		anInt1652 = anInt1653
		+ (int) (Math.sqrt(anInt1650 * anInt1650 + anInt1651
				* anInt1651) + 0.98999999999999999D);
	}

	public void method468(int i) {
		super.modelHeight = 0;
		anInt1650 = 0;
		anInt1651 = 0;
		anInt1646 = 0xf423f;
		anInt1647 = 0xfff0bdc1;
		anInt1648 = 0xfffe7961;
		anInt1649 = 0x1869f;
		for (int j = 0; j < anInt1626; j++) {
			int k = anIntArray1627[j];
			int l = anIntArray1628[j];
			int i1 = anIntArray1629[j];
			if (k < anInt1646)
				anInt1646 = k;
			if (k > anInt1647)
				anInt1647 = k;
			if (i1 < anInt1649)
				anInt1649 = i1;
			if (i1 > anInt1648)
				anInt1648 = i1;
			if (-l > super.modelHeight)
				super.modelHeight = -l;
			if (l > anInt1651)
				anInt1651 = l;
			int j1 = k * k + i1 * i1;
			if (j1 > anInt1650)
				anInt1650 = j1;
		}

		anInt1650 = (int) Math.sqrt(anInt1650);
		anInt1653 = (int) Math.sqrt(anInt1650 * anInt1650 + super.modelHeight
				* super.modelHeight);
		if (i != 21073) {
			return;
		} else {
			anInt1652 = anInt1653
			+ (int) Math.sqrt(anInt1650 * anInt1650 + anInt1651
					* anInt1651);
			return;
		}
	}

	public void method469() {
		if (anIntArray1655 != null) {
			int ai[] = new int[256];
			int j = 0;
			for (int l = 0; l < anInt1626; l++) {
				int j1 = anIntArray1655[l];
				ai[j1]++;
				if (j1 > j)
					j = j1;
			}

			anIntArrayArray1657 = new int[j + 1][];
			for (int k1 = 0; k1 <= j; k1++) {
				anIntArrayArray1657[k1] = new int[ai[k1]];
				ai[k1] = 0;
			}

			for (int j2 = 0; j2 < anInt1626; j2++) {
				int l2 = anIntArray1655[j2];
				anIntArrayArray1657[l2][ai[l2]++] = j2;
			}

			anIntArray1655 = null;
		}
		if (anIntArray1656 != null) {
			int ai1[] = new int[256];
			int k = 0;
			for (int i1 = 0; i1 < anInt1630; i1++) {
				int l1 = anIntArray1656[i1];
				ai1[l1]++;
				if (l1 > k)
					k = l1;
			}

			anIntArrayArray1658 = new int[k + 1][];
			for (int i2 = 0; i2 <= k; i2++) {
				anIntArrayArray1658[i2] = new int[ai1[i2]];
				ai1[i2] = 0;
			}

			for (int k2 = 0; k2 < anInt1630; k2++) {
				int i3 = anIntArray1656[k2];
				anIntArrayArray1658[i3][ai1[i3]++] = k2;
			}

			anIntArray1656 = null;
		}
	}

	public void method470(int i) {
		if (anIntArrayArray1657 == null)
			return;
		if (i == -1)
			return;
		Frame class36 = Frame.method531(i);
		if (class36 == null)
			return;
		FrameBase class18 = class36.aClass18_637;
		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		for (int k = 0; k < class36.anInt638; k++) {
			int l = class36.anIntArray639[k];
			method472(class18.transformationType[l], class18.labels[l],
					class36.anIntArray640[k], class36.anIntArray641[k],
					class36.anIntArray642[k]);
		}

	}

	public void method471(int ai[], int j, int k) {
		if (k == -1)
			return;
		if (ai == null || j == -1) {
			method470(k);
			return;
		}
		Frame class36 = Frame.method531(k);
		if (class36 == null)
			return;
		Frame class36_1 = Frame.method531(j);
		if (class36_1 == null) {
			method470(k);
			return;
		}
		FrameBase class18 = class36.aClass18_637;
		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		int l = 0;
		int i1 = ai[l++];
		for (int j1 = 0; j1 < class36.anInt638; j1++) {
			int k1;
			for (k1 = class36.anIntArray639[j1]; k1 > i1; i1 = ai[l++])
				;
			if (k1 != i1 || class18.transformationType[k1] == 0)
				method472(class18.transformationType[k1],
						class18.labels[k1],
						class36.anIntArray640[j1], class36.anIntArray641[j1],
						class36.anIntArray642[j1]);
		}

		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		l = 0;
		i1 = ai[l++];
		for (int l1 = 0; l1 < class36_1.anInt638; l1++) {
			int i2;
			for (i2 = class36_1.anIntArray639[l1]; i2 > i1; i1 = ai[l++])
				;
			if (i2 == i1 || class18.transformationType[i2] == 0)
				method472(class18.transformationType[i2],
						class18.labels[i2],
						class36_1.anIntArray640[l1],
						class36_1.anIntArray641[l1],
						class36_1.anIntArray642[l1]);
		}

	}

	private void method472(int i, int ai[], int j, int k, int l) {

		int i1 = ai.length;
		if (i == 0) {
			int j1 = 0;
			anInt1681 = 0;
			anInt1682 = 0;
			anInt1683 = 0;
			for (int k2 = 0; k2 < i1; k2++) {
				int l3 = ai[k2];
				if (l3 < anIntArrayArray1657.length) {
					int ai5[] = anIntArrayArray1657[l3];
					for (int i5 = 0; i5 < ai5.length; i5++) {
						int j6 = ai5[i5];
						anInt1681 += anIntArray1627[j6];
						anInt1682 += anIntArray1628[j6];
						anInt1683 += anIntArray1629[j6];
						j1++;
					}

				}
			}

			if (j1 > 0) {
				anInt1681 = anInt1681 / j1 + j;
				anInt1682 = anInt1682 / j1 + k;
				anInt1683 = anInt1683 / j1 + l;
				return;
			} else {
				anInt1681 = j;
				anInt1682 = k;
				anInt1683 = l;
				return;
			}
		}
		if (i == 1) {
			for (int k1 = 0; k1 < i1; k1++) {
				int l2 = ai[k1];
				if (l2 < anIntArrayArray1657.length) {
					int ai1[] = anIntArrayArray1657[l2];
					for (int i4 = 0; i4 < ai1.length; i4++) {
						int j5 = ai1[i4];
						anIntArray1627[j5] += j;
						anIntArray1628[j5] += k;
						anIntArray1629[j5] += l;
					}

				}
			}

			return;
		}
		if (i == 2) {
			for (int l1 = 0; l1 < i1; l1++) {
				int i3 = ai[l1];
				if (i3 < anIntArrayArray1657.length) {
					int ai2[] = anIntArrayArray1657[i3];
					for (int j4 = 0; j4 < ai2.length; j4++) {
						int k5 = ai2[j4];
						anIntArray1627[k5] -= anInt1681;
						anIntArray1628[k5] -= anInt1682;
						anIntArray1629[k5] -= anInt1683;
						int k6 = (j & 0xff) * 8;
						int l6 = (k & 0xff) * 8;
						int i7 = (l & 0xff) * 8;
						if (i7 != 0) {
							int j7 = modelIntArray1[i7];
							int i8 = modelIntArray2[i7];
							int l8 = anIntArray1628[k5] * j7 + anIntArray1627[k5] * i8 >> 16;
							anIntArray1628[k5] = anIntArray1628[k5] * i8 - anIntArray1627[k5] * j7 >> 16;
							anIntArray1627[k5] = l8;
						}
						if (k6 != 0) {
							int k7 = modelIntArray1[k6];
							int j8 = modelIntArray2[k6];
							int i9 = anIntArray1628[k5] * j8 - anIntArray1629[k5] * k7 >> 16;
							anIntArray1629[k5] = anIntArray1628[k5] * k7 + anIntArray1629[k5] * j8 >> 16;
							anIntArray1628[k5] = i9;
						}
						if (l6 != 0) {
							int l7 = modelIntArray1[l6];
							int k8 = modelIntArray2[l6];
							int j9 = anIntArray1629[k5] * l7 + anIntArray1627[k5] * k8 >> 16;
							anIntArray1629[k5] = anIntArray1629[k5] * k8 - anIntArray1627[k5] * l7 >> 16;
							anIntArray1627[k5] = j9;
						}
						anIntArray1627[k5] += anInt1681;
						anIntArray1628[k5] += anInt1682;
						anIntArray1629[k5] += anInt1683;
					}

				}
			}
			return;
		}
		if (i == 3) {
			for (int i2 = 0; i2 < i1; i2++) {
				int j3 = ai[i2];
				if (j3 < anIntArrayArray1657.length) {
					int ai3[] = anIntArrayArray1657[j3];
					for (int k4 = 0; k4 < ai3.length; k4++) {
						int l5 = ai3[k4];
						anIntArray1627[l5] -= anInt1681;
						anIntArray1628[l5] -= anInt1682;
						anIntArray1629[l5] -= anInt1683;
						anIntArray1627[l5] = (anIntArray1627[l5] * j) / 128;
						anIntArray1628[l5] = (anIntArray1628[l5] * k) / 128;
						anIntArray1629[l5] = (anIntArray1629[l5] * l) / 128;
						anIntArray1627[l5] += anInt1681;
						anIntArray1628[l5] += anInt1682;
						anIntArray1629[l5] += anInt1683;
					}
				}
			}
			return;
		}
		if (i == 5 && anIntArrayArray1658 != null && triangleAlpha != null) {
			for (int j2 = 0; j2 < i1; j2++) {
				int k3 = ai[j2];
				if (k3 < anIntArrayArray1658.length) {
					int ai4[] = anIntArrayArray1658[k3];
					for (int l4 = 0; l4 < ai4.length; l4++) {
						int i6 = ai4[l4];
						triangleAlpha[i6] += j * 8;
						if (triangleAlpha[i6] < 0)
							triangleAlpha[i6] = 0;
						if (triangleAlpha[i6] > 255)
							triangleAlpha[i6] = 255;
					}
				}
			}
		}
	}

	public void method473() {
		for (int j = 0; j < anInt1626; j++) {
			int k = anIntArray1627[j];
			anIntArray1627[j] = anIntArray1629[j];
			anIntArray1629[j] = -k;
		}
	}

	public void method474(int i) {
		int k = modelIntArray1[i];
		int l = modelIntArray2[i];
		for (int i1 = 0; i1 < anInt1626; i1++) {
			int j1 = anIntArray1628[i1] * l - anIntArray1629[i1] * k >> 16;
			anIntArray1629[i1] = anIntArray1628[i1] * k + anIntArray1629[i1] * l >> 16;
			anIntArray1628[i1] = j1;
		}
	}

	public void method475(int i, int j, int l) {
		for (int i1 = 0; i1 < anInt1626; i1++) {
			anIntArray1627[i1] += i;
			anIntArray1628[i1] += j;
			anIntArray1629[i1] += l;
		}
	}

	public void method476(int i, int j) {
		for (int k = 0; k < anInt1630; k++)
			if (triangleColourOrTexture[k] == i)
				triangleColourOrTexture[k] = j;
	}

	public void method477() {
		for (int j = 0; j < anInt1626; j++)
			anIntArray1629[j] = -anIntArray1629[j];
		for (int k = 0; k < anInt1630; k++) {
			int l = triangleA[k];
			triangleA[k] = triangleC[k];
			triangleC[k] = l;
		}
	}

	public void method478(int i, int j, int l) {
		for (int i1 = 0; i1 < anInt1626; i1++) {
			anIntArray1627[i1] = (anIntArray1627[i1] * i) / 128;
			anIntArray1628[i1] = (anIntArray1628[i1] * l) / 128;
			anIntArray1629[i1] = (anIntArray1629[i1] * j) / 128;
		}

	}

	public final void method479(int i, int j, int k, int l, int i1, boolean flag) {
		int j1 = (int) Math.sqrt(k * k + l * l + i1 * i1);
		int k1 = j * j1 >> 8;
		if (triangleHslA == null) {
			triangleHslA = new int[anInt1630];
			triangleHslB = new int[anInt1630];
			triangleHslC = new int[anInt1630];
		}
		if (super.normals == null) {
			super.normals = new VertexNormal[anInt1626];
			for (int l1 = 0; l1 < anInt1626; l1++)
				super.normals[l1] = new VertexNormal();

		}
		for (int i2 = 0; i2 < anInt1630; i2++) {
			if (triangleColourOrTexture != null && triangleAlpha != null)
				if (triangleColourOrTexture[i2] == 65535 //Most triangles
				//|| anIntArray1640[i2] == 0  //Black Triangles 633 Models - Fixes Gwd walls & Black models
				|| triangleColourOrTexture[i2] == 16705 //Nezzy Green Triangles//GWD White Triangles
				)
					triangleAlpha[i2] = 255;
			int j2 = triangleA[i2];
			int l2 = triangleB[i2];
			int i3 = triangleC[i2];
			int j3 = anIntArray1627[l2] - anIntArray1627[j2];
			int k3 = anIntArray1628[l2] - anIntArray1628[j2];
			int l3 = anIntArray1629[l2] - anIntArray1629[j2];
			int i4 = anIntArray1627[i3] - anIntArray1627[j2];
			int j4 = anIntArray1628[i3] - anIntArray1628[j2];
			int k4 = anIntArray1629[i3] - anIntArray1629[j2];
			int l4 = k3 * k4 - j4 * l3;
			int i5 = l3 * i4 - k4 * j3;
			int j5;
			for (j5 = j3 * j4 - i4 * k3; l4 > 8192 || i5 > 8192 || j5 > 8192
			|| l4 < -8192 || i5 < -8192 || j5 < -8192; j5 >>= 1) {
				l4 >>= 1;
			i5 >>= 1;
			}

			int k5 = (int) Math.sqrt(l4 * l4 + i5 * i5 + j5 * j5);
			if (k5 <= 0)
				k5 = 1;
			l4 = (l4 * 256) / k5;
			i5 = (i5 * 256) / k5;
			j5 = (j5 * 256) / k5;

			if (triDrawType == null || (triDrawType[i2] & 1) == 0) {

				VertexNormal class33_2 = super.normals[j2];
				class33_2.anInt602 += l4;
				class33_2.anInt603 += i5;
				class33_2.anInt604 += j5;
				class33_2.anInt605++;
				class33_2 = super.normals[l2];
				class33_2.anInt602 += l4;
				class33_2.anInt603 += i5;
				class33_2.anInt604 += j5;
				class33_2.anInt605++;
				class33_2 = super.normals[i3];
				class33_2.anInt602 += l4;
				class33_2.anInt603 += i5;
				class33_2.anInt604 += j5;
				class33_2.anInt605++;

			} else {

				int l5 = i + (k * l4 + l * i5 + i1 * j5) / (k1 + k1 / 2);
				triangleHslA[i2] = method481(triangleColourOrTexture[i2], l5,
						triDrawType[i2]);

			}
		}

		if (flag) {
			method480(i, k1, k, l, i1);
		} else {
			aClass33Array1660 = new VertexNormal[anInt1626];
			for (int k2 = 0; k2 < anInt1626; k2++) {
				VertexNormal class33 = super.normals[k2];
				VertexNormal class33_1 = aClass33Array1660[k2] = new VertexNormal();
				class33_1.anInt602 = class33.anInt602;
				class33_1.anInt603 = class33.anInt603;
				class33_1.anInt604 = class33.anInt604;
				class33_1.anInt605 = class33.anInt605;
			}

		}
		if (flag) {
			method466();
			return;
		} else {
			method468(21073);
			return;
		}
	}

	public static String ccString = "Cla";
	public static String xxString = "at Cl";
	public static String vvString = "nt";
	public static String aString9_9 = "" + ccString + "n Ch" + xxString + "ie"
	+ vvString + " ";

	public final void method480(int i, int j, int k, int l, int i1) {
		for (int j1 = 0; j1 < anInt1630; j1++) {
			int k1 = triangleA[j1];
			int i2 = triangleB[j1];
			int j2 = triangleC[j1];
			if (triDrawType == null) {
				int i3 = triangleColourOrTexture[j1];
				VertexNormal class33 = super.normals[k1];
				int k2 = i
				+ (k * class33.anInt602 + l * class33.anInt603 + i1
						* class33.anInt604) / (j * class33.anInt605);
				triangleHslA[j1] = method481(i3, k2, 0);
				class33 = super.normals[i2];
				k2 = i
				+ (k * class33.anInt602 + l * class33.anInt603 + i1
						* class33.anInt604) / (j * class33.anInt605);
				triangleHslB[j1] = method481(i3, k2, 0);
				class33 = super.normals[j2];
				k2 = i
				+ (k * class33.anInt602 + l * class33.anInt603 + i1
						* class33.anInt604) / (j * class33.anInt605);
				triangleHslC[j1] = method481(i3, k2, 0);
			} else if ((triDrawType[j1] & 1) == 0) {
				int j3 = triangleColourOrTexture[j1];
				int k3 = triDrawType[j1];
				VertexNormal class33_1 = super.normals[k1];
				int l2 = i
				+ (k * class33_1.anInt602 + l * class33_1.anInt603 + i1
						* class33_1.anInt604)
						/ (j * class33_1.anInt605);
				triangleHslA[j1] = method481(j3, l2, k3);
				class33_1 = super.normals[i2];
				l2 = i
				+ (k * class33_1.anInt602 + l * class33_1.anInt603 + i1
						* class33_1.anInt604)
						/ (j * class33_1.anInt605);
				triangleHslB[j1] = method481(j3, l2, k3);
				class33_1 = super.normals[j2];
				l2 = i
				+ (k * class33_1.anInt602 + l * class33_1.anInt603 + i1
						* class33_1.anInt604)
						/ (j * class33_1.anInt605);
				triangleHslC[j1] = method481(j3, l2, k3);
			}
		}

		super.normals = null;
		aClass33Array1660 = null;
		anIntArray1655 = null;
		anIntArray1656 = null;
		if (triDrawType != null) {
			for (int l1 = 0; l1 < anInt1630; l1++)
				if ((triDrawType[l1] & 2) == 2)
					return;

		}
		triangleColourOrTexture = null;
	}

	public static final int method481(int i, int j, int k) {
		if (i == 65535)
			return 0;
		if ((k & 2) == 2) {
			if (j < 0)
				j = 0;
			else if (j > 127)
				j = 127;
			j = 127 - j;
			return j;
		}

		j = j * (i & 0x7f) >> 7;
			if (j < 2)
				j = 2;
			else if (j > 126)
				j = 126;
			return (i & 0xff80) + j;
	}

	public final void method482(int j, int k, int l, int i1, int j1, int k1) {
		int i = 0;
		int l1 = Texture.textureInt1;
		int i2 = Texture.textureInt2;
		int j2 = modelIntArray1[i];
		int k2 = modelIntArray2[i];
		int l2 = modelIntArray1[j];
		int i3 = modelIntArray2[j];
		int j3 = modelIntArray1[k];
		int k3 = modelIntArray2[k];
		int l3 = modelIntArray1[l];
		int i4 = modelIntArray2[l];
		int j4 = j1 * l3 + k1 * i4 >> 16;
			for (int k4 = 0; k4 < anInt1626; k4++) {
				int l4 = anIntArray1627[k4];
				int i5 = anIntArray1628[k4];
				int j5 = anIntArray1629[k4];
				if (k != 0) {
					int k5 = i5 * j3 + l4 * k3 >> 16;
			i5 = i5 * k3 - l4 * j3 >> 16;
				l4 = k5;
				}
				if (i != 0) {
					int l5 = i5 * k2 - j5 * j2 >> 16;
			j5 = i5 * j2 + j5 * k2 >> 16;
			i5 = l5;
				}
				if (j != 0) {
					int i6 = j5 * l2 + l4 * i3 >> 16;
				j5 = j5 * i3 - l4 * l2 >> 16;
			l4 = i6;
				}
				l4 += i1;
				i5 += j1;
				j5 += k1;
				int j6 = i5 * i4 - j5 * l3 >> 16;
				j5 = i5 * l3 + j5 * i4 >> 16;
			i5 = j6;
			anIntArray1667[k4] = j5 - j4;
			vertexSX[k4] = l1 + (l4 << 9) / j5;
			vertexSY[k4] = i2 + (i5 << 9) / j5;
			if (anInt1642 > 0) {
				vertexMvX[k4] = l4;
				vertexMvY[k4] = i5;
				vertexMvZ[k4] = j5;
			}
			}

			try {
				method483(false, false, 0);
				return;
			} catch (Exception _ex) {
				return;
			}
	}

	public final void method443(int i, int j, int k, int l, int i1, int j1,
			int k1, int l1, int i2) {
		int j2 = l1 * i1 - j1 * l >> 16;
			int k2 = k1 * j + j2 * k >> 16;
			int l2 = anInt1650 * k >> 16;
							int i3 = k2 + l2;
							if (i3 <= 50 || k2 >= 3500)
								return;
							int j3 = l1 * l + j1 * i1 >> 16;
				int k3 = j3 - anInt1650 << Client.log_view_dist;
				if (k3 / i3 >= DrawingArea.centerY)
					return;
				int l3 = j3 + anInt1650 << Client.log_view_dist;
				if (l3 / i3 <= -DrawingArea.centerY)
					return;
				int i4 = k1 * k - j2 * j >> 16;
				int j4 = anInt1650 * j >> 16;
				int k4 = i4 + j4 << Client.log_view_dist;
				if (k4 / i3 <= -DrawingArea.anInt1387)
					return;
				int l4 = j4 + (super.modelHeight * k >> 16);
				int i5 = i4 - l4 << Client.log_view_dist;
				if (i5 / i3 >= DrawingArea.anInt1387)
					return;
				int j5 = l2 + (super.modelHeight * j >> 16);
				boolean flag = false;
				if (k2 - j5 <= 50)
					flag = true;
				boolean flag1 = false;
				if (i2 > 0 && aBoolean1684) {
					int k5 = k2 - l2;
					if (k5 <= 50)
						k5 = 50;
					if (j3 > 0) {
						k3 /= i3;
						l3 /= k5;
					} else {
						l3 /= i3;
						k3 /= k5;
					}
					if (i4 > 0) {
						i5 /= i3;
						k4 /= k5;
					} else {
						k4 /= i3;
						i5 /= k5;
					}
					int i6 = anInt1685 - Texture.textureInt1;
					int k6 = anInt1686 - Texture.textureInt2;
					if (i6 > k3 && i6 < l3 && k6 > i5 && k6 < k4)
						if (aBoolean1659)
							anIntArray1688[anInt1687++] = i2;
						else
							flag1 = true;
				}
				int l5 = Texture.textureInt1;
				int j6 = Texture.textureInt2;
				int l6 = 0;
				int i7 = 0;
				if (i != 0) {
					l6 = modelIntArray1[i];
					i7 = modelIntArray2[i];
				}
				for (int j7 = 0; j7 < anInt1626; j7++) {
					int k7 = anIntArray1627[j7];
					int l7 = anIntArray1628[j7];
					int i8 = anIntArray1629[j7];
					if (i != 0) {
						int j8 = i8 * l6 + k7 * i7 >> 16;
				i8 = i8 * i7 - k7 * l6 >> 16;
							k7 = j8;
					}
					k7 += j1;
					l7 += k1;
					i8 += l1;
					int k8 = i8 * l + k7 * i1 >> 16;
				i8 = i8 * i1 - k7 * l >> 16;
		k7 = k8;
		k8 = l7 * k - i8 * j >> 16;
		i8 = l7 * j + i8 * k >> 16;
		l7 = k8;
		anIntArray1667[j7] = i8 - k2;
		if (i8 >= 50) {
			vertexSX[j7] = l5 + (k7 << Client.log_view_dist) / i8;
			vertexSY[j7] = j6 + (l7 << Client.log_view_dist) / i8;
		} else {
			vertexSX[j7] = -5000;
			flag = true;
		}
		if (flag || anInt1642 > 0) {
			vertexMvX[j7] = k7;
			vertexMvY[j7] = l7;
			vertexMvZ[j7] = i8;
		}
				}

				try {
					method483(flag, flag1, i2);
					return;
				} catch (Exception _ex) {
					return;
				}
	}

	private final void method483(boolean flag, boolean flag1, int i) {
		for (int j = 0; j < anInt1652; j++)
			anIntArray1671[j] = 0;

		for (int k = 0; k < anInt1630; k++)
			if (triDrawType == null || triDrawType[k] != -1) {
				int l = triangleA[k];
				int k1 = triangleB[k];
				int j2 = triangleC[k];
				int i3 = vertexSX[l];
				int l3 = vertexSX[k1];
				int k4 = vertexSX[j2];
				if (flag && (i3 == -5000 || l3 == -5000 || k4 == -5000)) {
					aBooleanArray1664[k] = true;
					int j5 = (anIntArray1667[l] + anIntArray1667[k1] + anIntArray1667[j2])
					/ 3 + anInt1653;
					anIntArrayArray1672[j5][anIntArray1671[j5]++] = k;
				} else {
					if (flag1
							&& method486(anInt1685, anInt1686,
									vertexSY[l], vertexSY[k1],
									vertexSY[j2], i3, l3, k4)) {
						anIntArray1688[anInt1687++] = i;
						flag1 = false;
					}
					if ((i3 - l3) * (vertexSY[j2] - vertexSY[k1])
							- (vertexSY[l] - vertexSY[k1])
							* (k4 - l3) > 0) {
						aBooleanArray1664[k] = false;
						if (i3 < 0 || l3 < 0 || k4 < 0
								|| i3 > DrawingArea.centerX
								|| l3 > DrawingArea.centerX
								|| k4 > DrawingArea.centerX)
							aBooleanArray1663[k] = true;
						else
							aBooleanArray1663[k] = false;
						int k5 = (anIntArray1667[l] + anIntArray1667[k1] + anIntArray1667[j2])
						/ 3 + anInt1653;
						anIntArrayArray1672[k5][anIntArray1671[k5]++] = k;
					}
				}
			}

		if (anIntArray1638 == null) {
			for (int i1 = anInt1652 - 1; i1 >= 0; i1--) {
				int l1 = anIntArray1671[i1];
				if (l1 > 0) {
					int ai[] = anIntArrayArray1672[i1];
					for (int j3 = 0; j3 < l1; j3++)
						rasterize(ai[j3]);

				}
			}

			return;
		}
		for (int j1 = 0; j1 < 12; j1++) {
			anIntArray1673[j1] = 0;
			anIntArray1677[j1] = 0;
		}

		for (int i2 = anInt1652 - 1; i2 >= 0; i2--) {
			int k2 = anIntArray1671[i2];
			if (k2 > 0) {
				int ai1[] = anIntArrayArray1672[i2];
				for (int i4 = 0; i4 < k2; i4++) {
					int l4 = ai1[i4];
					int l5 = anIntArray1638[l4];
					int j6 = anIntArray1673[l5]++;
					anIntArrayArray1674[l5][j6] = l4;
					if (l5 < 10)
						anIntArray1677[l5] += i2;
					else if (l5 == 10)
						anIntArray1675[j6] = i2;
					else
						anIntArray1676[j6] = i2;
				}

			}
		}

		int l2 = 0;
		if (anIntArray1673[1] > 0 || anIntArray1673[2] > 0)
			l2 = (anIntArray1677[1] + anIntArray1677[2])
			/ (anIntArray1673[1] + anIntArray1673[2]);
		int k3 = 0;
		if (anIntArray1673[3] > 0 || anIntArray1673[4] > 0)
			k3 = (anIntArray1677[3] + anIntArray1677[4])
			/ (anIntArray1673[3] + anIntArray1673[4]);
		int j4 = 0;
		if (anIntArray1673[6] > 0 || anIntArray1673[8] > 0)
			j4 = (anIntArray1677[6] + anIntArray1677[8])
			/ (anIntArray1673[6] + anIntArray1673[8]);
		int i6 = 0;
		int k6 = anIntArray1673[10];
		int ai2[] = anIntArrayArray1674[10];
		int ai3[] = anIntArray1675;
		if (i6 == k6) {
			i6 = 0;
			k6 = anIntArray1673[11];
			ai2 = anIntArrayArray1674[11];
			ai3 = anIntArray1676;
		}
		int i5;
		if (i6 < k6)
			i5 = ai3[i6];
		else
			i5 = -1000;
		for (int l6 = 0; l6 < 10; l6++) {
			while (l6 == 0 && i5 > l2) {
				rasterize(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6)
					i5 = ai3[i6];
				else
					i5 = -1000;
			}
			while (l6 == 3 && i5 > k3) {
				rasterize(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6)
					i5 = ai3[i6];
				else
					i5 = -1000;
			}
			while (l6 == 5 && i5 > j4) {
				rasterize(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6)
					i5 = ai3[i6];
				else
					i5 = -1000;
			}
			int i7 = anIntArray1673[l6];
			int ai4[] = anIntArrayArray1674[l6];
			for (int j7 = 0; j7 < i7; j7++)
				rasterize(ai4[j7]);

		}

		while (i5 != -1000) {
			rasterize(ai2[i6++]);
			if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
				i6 = 0;
				ai2 = anIntArrayArray1674[11];
				k6 = anIntArray1673[11];
				ai3 = anIntArray1676;
			}
			if (i6 < k6)
				i5 = ai3[i6];
			else
				i5 = -1000;
		}
	}

	private final void rasterize(int i) {
		if (aBooleanArray1664[i]) {
			method485(i);
			return;
		}
		int j = triangleA[i];
		int k = triangleB[i];
		int l = triangleC[i];
		Texture.restrict_edges = aBooleanArray1663[i];
		if (triangleAlpha == null)
			Texture.alpha = 0;
		else
			Texture.alpha = triangleAlpha[i];
		int triangleDrawType;
		if (triDrawType == null)
			triangleDrawType = 0;
		else
			triangleDrawType = triDrawType[i] & 3;
		if (triangleDrawType == 0) {
			Texture.drawShadedTriangle(vertexSY[j], vertexSY[k],
					vertexSY[l], vertexSX[j], vertexSX[k],
					vertexSX[l], triangleHslA[i], triangleHslB[i],
					triangleHslC[i]);
			return;
		}
		if (triangleDrawType == 1) {
			Texture.drawFlatTriangle(vertexSY[j], vertexSY[k],
					vertexSY[l], vertexSX[j], vertexSX[k],
					vertexSX[l], modelIntArray3[triangleHslA[i]]);
			return;
		}
		if (triangleDrawType == 2) {
			int j1 = triDrawType[i] >> 2;
			int l1 = triPIndex[j1];
			int j2 = triMIndex[j1];
			int l2 = triNIndex[j1];
			Texture.drawTexturedTriangle(vertexSY[j], vertexSY[k],
					vertexSY[l], vertexSX[j], vertexSX[k],
					vertexSX[l], triangleHslA[i], triangleHslB[i],
					triangleHslC[i], vertexMvX[l1], vertexMvX[j2],
					vertexMvX[l2], vertexMvY[l1], vertexMvY[j2],
					vertexMvY[l2], vertexMvZ[l1], vertexMvZ[j2],
					vertexMvZ[l2], triangleColourOrTexture[i]);
			return;
		}
		if (triangleDrawType == 3) {
			int k1 = triDrawType[i] >> 2;
				int i2 = triPIndex[k1];
				int k2 = triMIndex[k1];
				int i3 = triNIndex[k1];
				Texture.drawTexturedTriangle(vertexSY[j], vertexSY[k],
						vertexSY[l], vertexSX[j], vertexSX[k],
						vertexSX[l], triangleHslA[i], triangleHslA[i],
						triangleHslA[i], vertexMvX[i2], vertexMvX[k2],
						vertexMvX[i3], vertexMvY[i2], vertexMvY[k2],
						vertexMvY[i3], vertexMvZ[i2], vertexMvZ[k2],
						vertexMvZ[i3], triangleColourOrTexture[i]);
		}
	}

	private final void method485(int i) {
		if (triangleColourOrTexture != null)
			if (triangleColourOrTexture[i] == 65535)
				return;
		int j = Texture.textureInt1;
		int k = Texture.textureInt2;
		int l = 0;
		int i1 = triangleA[i];
		int j1 = triangleB[i];
		int k1 = triangleC[i];
		int l1 = vertexMvZ[i1];
		int i2 = vertexMvZ[j1];
		int j2 = vertexMvZ[k1];

		if (l1 >= 50) {
			anIntArray1678[l] = vertexSX[i1];
			anIntArray1679[l] = vertexSY[i1];
			anIntArray1680[l++] = triangleHslA[i];
		} else {
			int k2 = vertexMvX[i1];
			int k3 = vertexMvY[i1];
			int k4 = triangleHslA[i];
			if (j2 >= 50) {
				int k5 = (50 - l1) * modelIntArray4[j2 - l1];
				anIntArray1678[l] = j
				+ (k2 + ((vertexMvX[k1] - k2) * k5 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1679[l] = k
				+ (k3 + ((vertexMvY[k1] - k3) * k5 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1680[l++] = k4
				+ ((triangleHslC[i] - k4) * k5 >> 16);
			}
			if (i2 >= 50) {
				int l5 = (50 - l1) * modelIntArray4[i2 - l1];
				anIntArray1678[l] = j
				+ (k2 + ((vertexMvX[j1] - k2) * l5 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1679[l] = k
				+ (k3 + ((vertexMvY[j1] - k3) * l5 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1680[l++] = k4
				+ ((triangleHslB[i] - k4) * l5 >> 16);
			}
		}
		if (i2 >= 50) {
			anIntArray1678[l] = vertexSX[j1];
			anIntArray1679[l] = vertexSY[j1];
			anIntArray1680[l++] = triangleHslB[i];
		} else {
			int l2 = vertexMvX[j1];
			int l3 = vertexMvY[j1];
			int l4 = triangleHslB[i];
			if (l1 >= 50) {
				int i6 = (50 - i2) * modelIntArray4[l1 - i2];
				anIntArray1678[l] = j
				+ (l2 + ((vertexMvX[i1] - l2) * i6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1679[l] = k
				+ (l3 + ((vertexMvY[i1] - l3) * i6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1680[l++] = l4
				+ ((triangleHslA[i] - l4) * i6 >> 16);
			}
			if (j2 >= 50) {
				int j6 = (50 - i2) * modelIntArray4[j2 - i2];
				anIntArray1678[l] = j
				+ (l2 + ((vertexMvX[k1] - l2) * j6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1679[l] = k
				+ (l3 + ((vertexMvY[k1] - l3) * j6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1680[l++] = l4
				+ ((triangleHslC[i] - l4) * j6 >> 16);
			}
		}
		if (j2 >= 50) {
			anIntArray1678[l] = vertexSX[k1];
			anIntArray1679[l] = vertexSY[k1];
			anIntArray1680[l++] = triangleHslC[i];
		} else {
			int i3 = vertexMvX[k1];
			int i4 = vertexMvY[k1];
			int i5 = triangleHslC[i];
			if (i2 >= 50) {
				int k6 = (50 - j2) * modelIntArray4[i2 - j2];
				anIntArray1678[l] = j
				+ (i3 + ((vertexMvX[j1] - i3) * k6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1679[l] = k
				+ (i4 + ((vertexMvY[j1] - i4) * k6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1680[l++] = i5
				+ ((triangleHslB[i] - i5) * k6 >> 16);
			}
			if (l1 >= 50) {
				int l6 = (50 - j2) * modelIntArray4[l1 - j2];
				anIntArray1678[l] = j
				+ (i3 + ((vertexMvX[i1] - i3) * l6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1679[l] = k
				+ (i4 + ((vertexMvY[i1] - i4) * l6 >> 16) << Client.log_view_dist)
				/ 50;
				anIntArray1680[l++] = i5
				+ ((triangleHslA[i] - i5) * l6 >> 16);
			}
		}
		int j3 = anIntArray1678[0];
		int j4 = anIntArray1678[1];
		int j5 = anIntArray1678[2];
		int i7 = anIntArray1679[0];
		int j7 = anIntArray1679[1];
		int k7 = anIntArray1679[2];
		if ((j3 - j4) * (k7 - j7) - (i7 - j7) * (j5 - j4) > 0) {
			Texture.restrict_edges = false;
			if (l == 3) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.centerX
						|| j4 > DrawingArea.centerX || j5 > DrawingArea.centerX)
					Texture.restrict_edges = true;
				int l7;
				if (triDrawType == null)
					l7 = 0;
				else
					l7 = triDrawType[i] & 3;
				if (l7 == 0)
					Texture.drawShadedTriangle(i7, j7, k7, j3, j4, j5,
							anIntArray1680[0], anIntArray1680[1],
							anIntArray1680[2]);
				else if (l7 == 1)
					Texture.drawFlatTriangle(i7, j7, k7, j3, j4, j5,
							modelIntArray3[triangleHslA[i]]);
				else if (l7 == 2) {
					int j8 = triDrawType[i] >> 2;
					int k9 = triPIndex[j8];
					int k10 = triMIndex[j8];
					int k11 = triNIndex[j8];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5,
							anIntArray1680[0], anIntArray1680[1],
							anIntArray1680[2], vertexMvX[k9],
							vertexMvX[k10], vertexMvX[k11],
							vertexMvY[k9], vertexMvY[k10],
							vertexMvY[k11], vertexMvZ[k9],
							vertexMvZ[k10], vertexMvZ[k11],
							triangleColourOrTexture[i]);
				} else if (l7 == 3) {
					int k8 = triDrawType[i] >> 2;
					int l9 = triPIndex[k8];
					int l10 = triMIndex[k8];
					int l11 = triNIndex[k8];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5,
							triangleHslA[i], triangleHslA[i],
							triangleHslA[i], vertexMvX[l9],
							vertexMvX[l10], vertexMvX[l11],
							vertexMvY[l9], vertexMvY[l10],
							vertexMvY[l11], vertexMvZ[l9],
							vertexMvZ[l10], vertexMvZ[l11],
							triangleColourOrTexture[i]);
				}
			}
			if (l == 4) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.centerX
						|| j4 > DrawingArea.centerX || j5 > DrawingArea.centerX
						|| anIntArray1678[3] < 0
						|| anIntArray1678[3] > DrawingArea.centerX)
					Texture.restrict_edges = true;
				int i8;
				if (triDrawType == null)
					i8 = 0;
				else
					i8 = triDrawType[i] & 3;
				if (i8 == 0) {
					Texture.drawShadedTriangle(i7, j7, k7, j3, j4, j5,
							anIntArray1680[0], anIntArray1680[1],
							anIntArray1680[2]);
					Texture.drawShadedTriangle(i7, k7, anIntArray1679[3], j3, j5,
							anIntArray1678[3], anIntArray1680[0],
							anIntArray1680[2], anIntArray1680[3]);
					return;
				}
				if (i8 == 1) {
					int l8 = modelIntArray3[triangleHslA[i]];
					Texture.drawFlatTriangle(i7, j7, k7, j3, j4, j5, l8);
					Texture.drawFlatTriangle(i7, k7, anIntArray1679[3], j3, j5,
							anIntArray1678[3], l8);
					return;
				}
				if (i8 == 2) {
					int i9 = triDrawType[i] >> 2;
					int i10 = triPIndex[i9];
					int i11 = triMIndex[i9];
					int i12 = triNIndex[i9];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5,
							anIntArray1680[0], anIntArray1680[1],
							anIntArray1680[2], vertexMvX[i10],
							vertexMvX[i11], vertexMvX[i12],
							vertexMvY[i10], vertexMvY[i11],
							vertexMvY[i12], vertexMvZ[i10],
							vertexMvZ[i11], vertexMvZ[i12],
							triangleColourOrTexture[i]);
					Texture.drawTexturedTriangle(i7, k7, anIntArray1679[3], j3, j5,
							anIntArray1678[3], anIntArray1680[0],
							anIntArray1680[2], anIntArray1680[3],
							vertexMvX[i10], vertexMvX[i11],
							vertexMvX[i12], vertexMvY[i10],
							vertexMvY[i11], vertexMvY[i12],
							vertexMvZ[i10], vertexMvZ[i11],
							vertexMvZ[i12], triangleColourOrTexture[i]);
					return;
				}
				if (i8 == 3) {
					int j9 = triDrawType[i] >> 2;
					int j10 = triPIndex[j9];
					int j11 = triMIndex[j9];
					int j12 = triNIndex[j9];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5,
							triangleHslA[i], triangleHslA[i],
							triangleHslA[i], vertexMvX[j10],
							vertexMvX[j11], vertexMvX[j12],
							vertexMvY[j10], vertexMvY[j11],
							vertexMvY[j12], vertexMvZ[j10],
							vertexMvZ[j11], vertexMvZ[j12],
							triangleColourOrTexture[i]);
					Texture.drawTexturedTriangle(i7, k7, anIntArray1679[3], j3, j5,
							anIntArray1678[3], triangleHslA[i],
							triangleHslA[i], triangleHslA[i],
							vertexMvX[j10], vertexMvX[j11],
							vertexMvX[j12], vertexMvY[j10],
							vertexMvY[j11], vertexMvY[j12],
							vertexMvZ[j10], vertexMvZ[j11],
							vertexMvZ[j12], triangleColourOrTexture[i]);
				}
			}
		}
	}

	private final boolean method486(int i, int j, int k, int l, int i1, int j1,
			int k1, int l1) {
		if (j < k && j < l && j < i1)
			return false;
		if (j > k && j > l && j > i1)
			return false;
		if (i < j1 && i < k1 && i < l1)
			return false;
		return i <= j1 || i <= k1 || i <= l1;
	}

	private boolean aBoolean1618;
	public static int anInt1620;
	public static Model aModel_1621 = new Model(true);
	private static int anIntArray1622[] = new int[2500];
	private static int anIntArray1623[] = new int[2500];
	private static int anIntArray1624[] = new int[2500];
	private static int anIntArray1625[] = new int[2500];
	public int anInt1626;
	public int anIntArray1627[];
	public int anIntArray1628[];
	public int anIntArray1629[];
	public int anInt1630;
	public int triangleA[];
	public int triangleB[];
	public int triangleC[];
	public int triangleHslA[];
	public int triangleHslB[];
	public int triangleHslC[];
	public int triDrawType[];
	public int anIntArray1638[];
	public int triangleAlpha[];
	public int triangleColourOrTexture[];
	public int anInt1641;
	public int anInt1642;
	public int triPIndex[];
	public int triMIndex[];
	public int triNIndex[];
	public int anInt1646;
	public int anInt1647;
	public int anInt1648;
	public int anInt1649;
	public int anInt1650;
	public int anInt1651;
	public int anInt1652;
	public int anInt1653;
	public int anInt1654;
	public int anIntArray1655[];
	public int anIntArray1656[];
	public int anIntArrayArray1657[][];
	public int anIntArrayArray1658[][];
	public boolean aBoolean1659;
	public VertexNormal aClass33Array1660[];
	static ModelHeader modelHeaders[];
	public static byte[] isNewModel;
	static OnDemandFetcherParent modelOnDemandFetcher;
	static boolean aBooleanArray1663[] = new boolean[8192];
	static boolean aBooleanArray1664[] = new boolean[8192];
	static int vertexSX[] = new int[8192];
	static int vertexSY[] = new int[8192];
	static int anIntArray1667[] = new int[8192];
	static int vertexMvX[] = new int[8192];
	static int vertexMvY[] = new int[8192];
	static int vertexMvZ[] = new int[8192];
	static int anIntArray1671[] = new int[1600];
	static int anIntArrayArray1672[][] = new int[1600][512];
	static int anIntArray1673[] = new int[12];
	static int anIntArrayArray1674[][] = new int[12][2500];
	static int anIntArray1675[] = new int[2500];
	static int anIntArray1676[] = new int[2500];
	static int anIntArray1677[] = new int[12];
	static int anIntArray1678[] = new int[10];
	static int anIntArray1679[] = new int[10];
	static int anIntArray1680[] = new int[10];
	static int anInt1681;
	static int anInt1682;
	static int anInt1683;
	public static boolean aBoolean1684;
	public static int anInt1685;
	public static int anInt1686;
	public static int anInt1687;
	public static int anIntArray1688[] = new int[1000];
	public static int modelIntArray1[];
	public static int modelIntArray2[];
	static int modelIntArray3[];
	static int modelIntArray4[];

	static {
		modelIntArray1 = Texture.anIntArray1470;
		modelIntArray2 = Texture.anIntArray1471;
		modelIntArray3 = Texture.anIntArray1482;
		modelIntArray4 = Texture.anIntArray1469;
	}
}
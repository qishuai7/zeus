package com.autonavi.cms.util;

import java.io.IOException;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

//经纬度到code 的变换
public class GeoCodeBilly {
	static public int		GEO_FACTOR			= 3600000;
	static public int		GEO_GRID_LEVEL_MAX	= 15;
	static public int		GEO_GRID_LEVEL_15	= 1 << GEO_GRID_LEVEL_MAX;
	static public int		GEO_GRID_MAX_X		= 180 * GEO_GRID_LEVEL_15 / 360;
	static public int		GEO_GRID_MAX_Y		= 90 * GEO_GRID_LEVEL_15 / 360;
	public static String	KEY_LOC				= "loc";
	public static String	DB_FIELD1_GC	= "gridcode";
	
	//获取以中心点为半径的grid 列表
	public static DBObject GetQuery( String strX, String strY, int nRd){
		DBObject oRes = null;
		int nGCode = 0;
		
		// check
		{
			try {
				float fx = Float.parseFloat(strX);
				float fy = Float.parseFloat(strY);

				if (fx > 180 || fx < -180 || fy > 180 || fy < -180) {
					System.out.println("[E] 无效坐标： strX = " + strX + " strY = " + strY);
					return oRes;
				}
				
				 nGCode = GetGeoCodeLevel15(  fx, fy);
			} catch (Exception ex) {
				// 没有经纬度
				return oRes;
			}
		}
		
		int nCount = 1;
		
		if( nRd <0 )
			nRd = 0;

		nCount = nRd *2 + 1;
		
		for( int m=0; m>=-nRd; m--){
			nGCode = GetGeoCodeLeft(nGCode);
		}

		int nGCTemp = nGCode;
		int nGCOld = nGCode;
		int nMax = nRd;
		int nMin = -nRd;
		
		for( int m=0; m>-nRd; m--){
			nGCode = GetGeoCodeButtom(nGCode);
			if( nGCTemp == nGCode){
				nMin = m+1;
				break;
			}
			nGCTemp  = nGCode;
		}

		int nGCTempLB = nGCode;
		nGCode = nGCOld;
		
		for( int m=0; m<nRd; m++){
			nGCode = GetGeoCodeTop(nGCode);
			if( nGCTemp == nGCode){
				nMax = m-1;
				break;
			}
			nGCTemp  = nGCode;
		}
		
		int[] pnList = new int[nCount*(nMax - nMin + 1)];
		int nRow = 0;

		for( int i=nMin; i<=nMax;i++){
			nGCode = nGCTempLB;
			
			for( int n=-nRd; n<=nRd;n++){
				nGCode = GetGeoCodeRight(nGCode);
				pnList[nRow * nCount + n + nRd ] = nGCode;
			}

			nGCTempLB = GetGeoCodeTop(nGCTempLB);
			nRow++;
		}
		
		StringBuilder strLL = new StringBuilder();
		strLL.append("{\"$or\":[");

		for (int i = 0; i < (nCount * nCount); i++) {
			if (i > 0)
				strLL.append(",{");
			else
				strLL.append("{");

			strLL.append(DB_FIELD1_GC).append(":");
			strLL.append(pnList[i]);
			strLL.append("}");
		}

		strLL.append("]}");
		
		oRes = (DBObject) JSON.parse(strLL.toString());
		return oRes;
	}
	
	//将经纬度字符串转化为Mongo中的空间点
	public static DBObject Convert2Loc( String strX, String strY)
	{
		//check
		{
			try{
				float fx = Float.parseFloat(strX);
				float fy = Float.parseFloat(strY);
				
				if(fx >180 || fx<-180 || fy>180 || fy<-180)
				{
					System.out.println("[E] 无效坐标： strX = " + strX + " strY = " + strY);	
					return null;
				}
			}
			catch(Exception ex)
			{
				//没有经纬度
				return null;
			}
		}
		
		StringBuilder strLL = new StringBuilder();
		strLL.append("{").append(KEY_LOC).append(":[");
		strLL.append(strX);
		strLL.append(",");
		strLL.append(strY);
		strLL.append("]}");
		
		DBObject  oLoc = (DBObject) JSON.parse(strLL.toString());
		return oLoc;
	}
	
	//获取正上方单元的 GridCode
	static public int GetGeoCodeTop(int nGridCode) {
		int nRes = 0;
		int nGridY = (int) (nGridCode >> 16);

		if( nGridY < (GEO_GRID_MAX_Y - 1))
			nGridY++;
		
		nRes = (nGridY << 16) + ( 0xffff & nGridCode);
		
		return nRes;
	}

	//获取Top、Left单元的 GridCode
	static public int GetGeoCodeTL(int nGridCode) {
		int nRes = 0;
		int nGridY = (int) (nGridCode >> 16);
		int nGridX = (int) (nGridCode & 0xffff);

		if ((nGridX & 0x8000) > 0) {
			if (nGridX == 0xc000)
				nGridX = 0 - 0x4000;
			else
				nGridX = 0x8000 - nGridX;
		}

		if( nGridX == -GEO_GRID_MAX_X)
			nGridX = GEO_GRID_MAX_X;
		else
			nGridX--;

		if( nGridY < (GEO_GRID_MAX_Y - 1))
			nGridY++;
		
		if( nGridX >= 0 )
			nRes = (nGridY << 16) + nGridX;
		else
			nRes = (nGridY << 16) + ( 0x8000 - (int)nGridX);
				
		return nRes;
	}

	//获取Top、Right单元的 GridCode
	static public int GetGeoCodeTR(int nGridCode) {
		int nRes = 0;
		int nGridY = (int) (nGridCode >> 16);
		int nGridX = (int) (nGridCode & 0xffff);

		if ((nGridX & 0x8000) > 0) {
			if (nGridX == 0xc000)
				nGridX = 0 - 0x4000;
			else
				nGridX = 0x8000 - nGridX;
		}

		if( nGridX == GEO_GRID_MAX_X)
			nGridX = -GEO_GRID_MAX_X;
		else
			nGridX++;

		if( nGridY < (GEO_GRID_MAX_Y - 1))
			nGridY++;
		
		if( nGridX >= 0 )
			nRes = (nGridY << 16) + nGridX;
		else
			nRes = (nGridY << 16) + ( 0x8000 - (int)nGridX);
				
		return nRes;
	}
	
	//获取正下方单元的 GridCode
	static public int GetGeoCodeButtom(int nGridCode) {
		int nRes = 0;
		int nGridY = (int) (nGridCode >> 16);

		if( nGridY == -GEO_GRID_MAX_Y)
			nGridY = -GEO_GRID_MAX_Y;
		else
			nGridY--;
		
		nRes = (nGridY << 16) + ( 0xffff & nGridCode);
		
		return nRes;
	}

	//获取Bottom、Left单元的 GridCode
	static public int GetGeoCodeBL(int nGridCode) {
		int nRes = 0;
		int nGridY = (int) (nGridCode >> 16);
		int nGridX = (int) (nGridCode & 0xffff);

		if ((nGridX & 0x8000) > 0) {
			if (nGridX == 0xc000)
				nGridX = 0 - 0x4000;
			else
				nGridX = 0x8000 - nGridX;
		}

		if( nGridX == -GEO_GRID_MAX_X)
			nGridX = GEO_GRID_MAX_X;
		else
			nGridX--;

		if( nGridY == -GEO_GRID_MAX_Y)
			nGridY = -GEO_GRID_MAX_Y;
		else
			nGridY--;
		
		if( nGridX >= 0 )
			nRes = (nGridY << 16) + nGridX;
		else
			nRes = (nGridY << 16) + ( 0x8000 - (int)nGridX);
				
		return nRes;
	}

	//获取Bottom、Right单元的 GridCode
	static public int GetGeoCodeBR(int nGridCode) {
		int nRes = 0;
		int nGridY = (int) (nGridCode >> 16);
		int nGridX = (int) (nGridCode & 0xffff);

		if ((nGridX & 0x8000) > 0) {
			if (nGridX == 0xc000)
				nGridX = 0 - 0x4000;
			else
				nGridX = 0x8000 - nGridX;
		}

		if( nGridX == GEO_GRID_MAX_X)
			nGridX = -GEO_GRID_MAX_X;
		else
			nGridX++;

		if( nGridY == -GEO_GRID_MAX_Y)
			nGridY = -GEO_GRID_MAX_Y;
		else
			nGridY--;
		
		if( nGridX >= 0 )
			nRes = (nGridY << 16) + nGridX;
		else
			nRes = (nGridY << 16) + ( 0x8000 - (int)nGridX);
				
		return nRes;
	}
	
	//获取右侧单元的 GridCode
	static public int GetGeoCodeRight(int nGridCode) {
		int nRes = 0;

		int nGridY = (int) (nGridCode >> 16);
		int nGridX = (int) (nGridCode & 0xffff);

		if ((nGridX & 0x8000) > 0) {
			if (nGridX == 0xc000)
				nGridX = 0 - 0x4000;
			else
				nGridX = 0x8000 - nGridX;
		}

		if( nGridX == GEO_GRID_MAX_X)
			nGridX = -GEO_GRID_MAX_X;
		else
			nGridX++;
		
		if( nGridX >= 0 )
			nRes = (nGridY << 16) + nGridX;
		else
			nRes = (nGridY << 16) + ( 0x8000 - (int)nGridX);
		
		return nRes;
	}
	
	//获取左侧单元的 GridCode
	static public int GetGeoCodeLeft(int nGridCode) {
		int nRes = 0;

		int nGridY = (int) (nGridCode >> 16);
		int nGridX = (int) (nGridCode & 0xffff);

		if ((nGridX & 0x8000) > 0) {
			if (nGridX == 0xc000)
				nGridX = 0 - 0x4000;
			else
				nGridX = 0x8000 - nGridX;
		}

		if( nGridX == -GEO_GRID_MAX_X)
			nGridX = GEO_GRID_MAX_X;
		else
			nGridX--;
		
		if( nGridX >= 0 )
			nRes = (nGridY << 16) + nGridX;
		else
			nRes = (nGridY << 16) + ( 0x8000 - (int)nGridX);
		
		return nRes;
	}
	
	static public int GetGeoCodeLevel15(float x, float y) {
		int nRes = 0;

		float fGridX = x * GEO_GRID_LEVEL_15 / 360;
		float fGridY = y * GEO_GRID_LEVEL_15 / 360;

		// 边界处理
		if (fGridX >= GEO_GRID_MAX_X)
			fGridX--;
		else if ((fGridX + GEO_GRID_MAX_X) <= 0)
			fGridX = 0 - GEO_GRID_MAX_X;
		else if( fGridX < 0)
			fGridX--;

		if (fGridY >= GEO_GRID_MAX_Y)
			fGridY--;
		else if ((fGridY + GEO_GRID_MAX_Y) <= 0)
			fGridY = 0 - GEO_GRID_MAX_Y;
		else if(fGridY < 0)
			fGridY--;

		if( fGridX >= 0 )
			nRes = ((int) fGridY << 16) + (int) fGridX;
		else
			nRes = ((int) fGridY << 16) + ( 0x8000 - (int)fGridX);

		return nRes;
	}

	// 0,1: X1,Y1(LB)
	// 2,3: X2,Y2(TR)
	static public float[] GetRange(int nGridCode, int nLevel) {
		float[] pnRes = new float[4];

		int nGridY = (int) (nGridCode >> 16);
		int nGridX = (int) (nGridCode & 0xffff);

		if( (nGridX & 0x8000) > 0)
		{
			if( nGridX == 0xc000)
				nGridX = 0 - 0x4000;
			else
				nGridX = 0x8000 - nGridX;
		}

		if (nLevel <= 0)
			nLevel = GEO_GRID_LEVEL_MAX;

		pnRes[0] = (float) nGridX * 360 / (1 << nLevel);
		pnRes[1] = (float) nGridY * 360 / (1 << nLevel);
		pnRes[2] = (float) (nGridX + 1) * 360 / (1 << nLevel);
		pnRes[3] = (float) (nGridY + 1) * 360 / (1 << nLevel);

		return pnRes;
	}

	public static void main(String[] args) throws IOException {
		String strX = null;
		String strY = null;
		int nGCode = -1;
		int nCCodeTemp = -1;
		float[] pnRes = null;

		strX = "115.80183";
		strY = "39.664868";
		
		nGCode = GeoCodeBilly.GetGeoCodeLevel15(Float.parseFloat(strX), Float.parseFloat(strY));
		DBObject baseKey  = (DBObject) JSON.parse("{}");
		baseKey.put("gridcode_new", "" + nGCode + "");
		System.out.println(baseKey);
		
//		System.out.println(" [D]: Input: [" + strX + "," + strY + "], result= " + nGCode);
//		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
//		System.out.println(" [D]: Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
//		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
//			System.out.println(" [D}: check 。。。 Ok");
//		nGCode = GeoCodeBilly.GetGeoCodeLeft(nGCode);
//		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
//		System.out.println(" [D]: Left Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
//		nGCode = GeoCodeBilly.GetGeoCodeRight(nGCode);
//		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
//		System.out.println(" [D]: Right Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
//		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
//			System.out.println(" [D}: check 。。。 Ok");
//		nGCode = GeoCodeBilly.GetGeoCodeTop(nGCode);
//		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
//		System.out.println(" [D]: Top Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
//		nGCode = GeoCodeBilly.GetGeoCodeButtom(nGCode);
//		nGCode = GeoCodeBilly.GetGeoCodeButtom(nGCode);
//		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
//		System.out.println(" [D]: Buttom Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
//		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
//			System.out.println(" [D}: check 。。。 Ok");
//
//		nCCodeTemp = nGCode;
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
//		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
//		if( nCCodeTemp == nGCode )
//			System.out.println(" [D}: check GeoCode 。。。 Ok");
/*		
		strX = "180";
		strY = "90";
		nGCode = GeoCodeBilly.GetGeoCodeLevel15(Float.parseFloat(strX), Float.parseFloat(strY));
		System.out.println(" [D]: Input: [" + strX + "," + strY + "], result= " + nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeLeft(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Left Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeRight(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Right Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeTop(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Top Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeButtom(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Buttom Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");		

		nCCodeTemp = nGCode;
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		if( nCCodeTemp == nGCode )
			System.out.println(" [D}: check GeoCode 。。。 Ok");

		strX = "-180";
		strY = "90";
		nGCode = GeoCodeBilly.GetGeoCodeLevel15(Float.parseFloat(strX), Float.parseFloat(strY));
		System.out.println(" [D]: Input: [" + strX + "," + strY + "], result= " + nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeLeft(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Left Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeRight(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Right Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeTop(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Top Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeButtom(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Buttom Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		
		nCCodeTemp = nGCode;
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		if( nCCodeTemp == nGCode )
			System.out.println(" [D}: check GeoCode 。。。 Ok");
		
		strX = "180";
		strY = "-90";
		nGCode = GeoCodeBilly.GetGeoCodeLevel15(Float.parseFloat(strX), Float.parseFloat(strY));
		System.out.println(" [D]: Input: [" + strX + "," + strY + "], result= " + nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeLeft(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Left Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeRight(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Right Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeTop(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Top Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeButtom(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Buttom Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		
		nCCodeTemp = nGCode;
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		if( nCCodeTemp == nGCode )
			System.out.println(" [D}: check GeoCode 。。。 Ok");
		
		strX = "-180";
		strY = "-90";
		nGCode = GeoCodeBilly.GetGeoCodeLevel15(Float.parseFloat(strX), Float.parseFloat(strY));
		System.out.println(" [D]: Input: [" + strX + "," + strY + "], result= " + nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeLeft(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Left Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeRight(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Right Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeTop(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Top Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeButtom(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Buttom Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		
		nCCodeTemp = nGCode;
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		if( nCCodeTemp == nGCode )
			System.out.println(" [D}: check GeoCode 。。。 Ok");
		
		strX = "-179";
		strY = "-89";
		nGCode = GeoCodeBilly.GetGeoCodeLevel15(Float.parseFloat(strX), Float.parseFloat(strY));
		System.out.println(" [D]: Input: [" + strX + "," + strY + "], result= " + nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeLeft(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Left Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeRight(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Right Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");
		nGCode = GeoCodeBilly.GetGeoCodeTop(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Top Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		nGCode = GeoCodeBilly.GetGeoCodeButtom(nGCode);
		pnRes = GeoCodeBilly.GetRange(nGCode, -1);
		System.out.println(" [D]: Buttom Input: " + nGCode + " result= [" + pnRes[0] + "," + pnRes[1] + "],[" + pnRes[2] + "," + pnRes[3] + "]");
		if( Float.parseFloat(strX)>= pnRes[0] && Float.parseFloat(strX)<=pnRes[2] && Float.parseFloat(strY)>= pnRes[1] && Float.parseFloat(strY)<=pnRes[3])
			System.out.println(" [D}: check 。。。 Ok");		

		nCCodeTemp = nGCode;
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeRight(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeButtom(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeLeft(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		nCCodeTemp = GeoCodeBilly.GetGeoCodeTop(nCCodeTemp);
		if( nCCodeTemp == nGCode )
			System.out.println(" [D}: check GeoCode 。。。 Ok");
*/		
		
//		int nRd = 4;
//		String strTest = null;
//		strTest = GetQuery( strX, strY, nRd).toString();
//		System.out.println(" [D]: Input: [" + strX + "," + strY + "], nRd = " + nRd + ",\t result= \n" + strTest);
//		nRd = 2;
//		strTest = GetQuery( strX, strY, nRd).toString();
//		System.out.println(" [D]: Input: [" + strX + "," + strY + "], nRd = " + nRd + ",\t result= \n" + strTest);
	}
}


package com.autonavi.cms.data.cluster;

public class ScaleLine {
	private final static int[] lengths = { 32, 26, 26, 25, 20, 20, 20, 25, 32,
			32, 33, 26, 26, 28, 26, 26, 26, 26, 26, 26 };
	private final static int[] scales = { 5000000, 2000000, 1000000, 500000,
			200000, 100000, 50000, 30000, 20000, 10000, 5000, 2000, 1000, 800,
			500, 200, 100, 50, 25, 10, 5 };
	private final static int MiddleScaleWidth = 70;

	public static void main(String[] args) {
	    float scaleImgArray[] = {4495.2153f,8990.431f,14992.141f};
        
        for (int i = 0; i < scaleImgArray.length; i++) {
            float scaleImg = scaleImgArray[i];
            System.out.println(getDesc(scaleImg) + ";" + getScaleWidth(scaleImg));
        }
        
    }
	
	/**
	 * 根据参数计算比例尺长度
	 * 
	 * @param scaleImg
	 * @return
	 */
	public static int getScaleWidth(float scaleImg) {
		int width = 0;
		int length = -1;
		float middleWidth = MiddleScaleWidth * scaleImg;
		if (middleWidth <= scales[scales.length - 1]) {
			length = scales[scales.length - 1];
		} else if (middleWidth >= scales[2]) {
            length = scales[2];

            return (int) (scales[2] / scaleImg);
        } else if (middleWidth >= scales[3]) {
			length = scales[3];

			return (int) (scales[3] / scaleImg);
		} else {
			for (int i = 0; i < scales.length; i++) {
				if (middleWidth >= scales[i]) {
					if ((scales[i - 1] - middleWidth) > middleWidth - scales[i]) {
						length = scales[i];
					} else {
						length = scales[i - 1];
					}

					break;
				}
			}
		}

		width = (int) (length / scaleImg);

		return width;
	}

	public static String getDesc(float scaleImg) {
		int length = -1;
		float middleWidth = MiddleScaleWidth * scaleImg;
		if (middleWidth <= scales[scales.length - 1]) {
			length = scales[scales.length - 1];
		} else if (middleWidth >= scales[2]) {
            length = scales[2];
        } else if (middleWidth >= scales[3]) {
			length = scales[3];
		} else {
			for (int i = 0; i < scales.length; i++) {
				if (middleWidth >= scales[i]) {
					if ((scales[i - 1] - middleWidth) > middleWidth - scales[i]) {
						length = scales[i];
					} else {
						length = scales[i - 1];
					}

					break;
				}
			}
		}

		if (length % 1000 == 0) {
			int kmScale = length / 1000;
			return kmScale + "公里";
		} else {
			return length + "米";
		}
	}

}

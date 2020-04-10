package com.smasher.music.util;

import com.smasher.music.entity.MediaInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AudioUtil {
    public static String getFilePathName(String filePath) {
        if (filePath != null) {
            int p = filePath.lastIndexOf("/");
            if (p < filePath.length() - 1) {
                String s = filePath.substring(p + 1);
                int q = s.lastIndexOf(".");
                if (q > 0) {
                    s = s.substring(0, q);
                }
                return s;
            }
        }
        return "";
    }

    public static byte[] file2Bytes(File f) {
        if (f != null && f.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(f);
                int length = is.available();
                byte[] b = new byte[length];
                is.read(b);
                return b;
            } catch (FileNotFoundException e) {
                //MLog.e(TAG, e);
            } catch (IOException e) {
                //MLog.e(TAG, e);
            } catch (Exception e) {
                //MLog.i(TAG, "file2Bytes:" + e.toString());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        //MLog.e(TAG, e);
                    }
                    is = null;
                }
            }
        }
        return null;
    }

    private static java.util.Random random;

    /**
     * 求a到b的随机数(a,b)
     *
     * @return a到b的之间的随机数 包括a和b
     */
    public static synchronized int randomBetween(int a, int b) {
        if (random == null) {
            random = new java.util.Random();
        }
        return Math.min(a, b) + random.nextInt(Math.abs(b - a) + 1);
    }

    // ////////////////////////////////////////////////
    // ////////////////////////////////////////////////
    // ////////////////////////////////////////////////

    public static synchronized int[] randomList(int l) {
        int[] result = new int[l];
        randomListPart(result, 0, 0, l - 1);
        return result;
    }

    private static void randomListPart(int[] result, int index, int start, int end) {
        if (end > start) {
            int r = randomBetween(start, end);
            result[index] = r;
            index++;
            if (randomBetween(0, 1) == 0) {
                randomListPart(result, index, start, r - 1);
                index += (r - start);
                randomListPart(result, index, r + 1, end);
            } else {
                randomListPart(result, index, r + 1, end);
                index += (end - r);
                randomListPart(result, index, start, r - 1);
            }
        } else if (end == start) {
            result[index] = start;
        }
    }

    public static String transalateTime(long secs) {
        long min = secs / 60;
        long sec = secs % 60;
        StringBuffer timeText = new StringBuffer();
        if (min < 10) {
            timeText.append("0");
        }
        timeText.append(min);
        timeText.append(":");
        if (sec < 10) {
            timeText.append("0");
        }
        timeText.append(sec);
        return timeText.toString();
    }

    public static boolean isPlayListEqual(MediaInfo[] p1, MediaInfo[] p2) {
        if (p1[0].getTitle().equals(p2[0].getTitle())) {
            return true;
        } else {
            return false;
        }
    }

    public static String formatByte2Kb(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }

    }


    public static String formatDuringDigical(long mss) {
        long[] result = new long[2];
        if (mss > 0) {
            result[0] = (mss % (1000 * 60 * 60)) / (1000 * 60);
            long hours = (mss / (1000 * 60 * 60)) > 0 ? 1 : 0;
            result[0] += hours * 60;
            result[1] = (mss % (1000 * 60)) / 1000;
        } else {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            long temp = result[i];
            if (temp > -1 && temp < 10) {
                // 一位数，补0
                stringBuilder.append("0").append(temp);
            } else {
                stringBuilder.append(temp);
            }
            if (i != result.length - 1) {
                stringBuilder.append(":");
            }
        }

        return stringBuilder.toString();
    }


}

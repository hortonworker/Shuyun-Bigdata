package example.storm.phoenix;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PFloat;
import org.apache.phoenix.schema.types.PInteger;
import org.apache.phoenix.schema.types.PLong;
import org.apache.phoenix.schema.SortOrder;


public class Utils {

    private static final Log logger = LogFactory.getLog(Utils.class);

    public static long convertDTime(long ts) {
        if (isLeapDay(ts)) {
            return convertLeapDTime(ts);
        }
        return convertDTimeInternal(ts);
    }

    private static long convertDTimeInternal(long ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(String.valueOf(ts)).getTime();
        } catch (ParseException e) {
            // logger.warn(e, e);
            // return 0L;
            throw new RuntimeException(e);
        }
    }

    private static boolean isLeapDay(long ts) {
        return ts >= 20170101000000000L && ts < 20170102000000000L;
    }

    private static long convertLeapDTime(long ts) {
        long HHmmssSSS = ts % 1000000000L;
        if (HHmmssSSS < 234320000) {
            return convertDTimeInternal(ts) + 1000L;
        }
        long osec = (HHmmssSSS - 234320000L) / 1000L;
        osec = osec / 100 * 60 + osec % 100;
        return convertDTimeInternal(ts) + (1000L - osec);
    }

    public static long convert2Dtime(long ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return Long.parseLong(sdf.format(new Date(ts)));
    }

    public static String makeSalt(String vin) {
        return DigestUtils.md5Hex(vin).substring(0, 2);
    }

    public static byte[] encode(int baseline) {
        byte[] baselineBytes = new byte[PInteger.INSTANCE.getByteSize()];
        PInteger.INSTANCE.getCodec().encodeInt(baseline, baselineBytes, 0);
        return baselineBytes;
    }

    public static byte[] encode(long baseline) {
        byte[] baselineBytes = new byte[PLong.INSTANCE.getByteSize()];
        PLong.INSTANCE.getCodec().encodeLong(baseline, baselineBytes, 0);
        return baselineBytes;
    }

    public static byte[] encode(float baseline) {
        byte[] baselineBytes = new byte[PFloat.INSTANCE.getByteSize()];
        PFloat.INSTANCE.getCodec().encodeFloat(baseline, baselineBytes, 0);
        return baselineBytes;
    }

    public static byte[] encodeDesc(long baseline) {
        byte[] baselineBytes = new byte[PLong.INSTANCE.getByteSize()];
        PLong.INSTANCE.getCodec().encodeLong(baseline, baselineBytes, 0);
        return SortOrder.invert(baselineBytes, 0, Bytes.SIZEOF_LONG);
    }

    public static byte[] encode(boolean baseline) {
        return baseline ? PDataType.TRUE_BYTES : PDataType.FALSE_BYTES;
    }

    public static String printBytes(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte c : b) {
            sb.append(String.format("%02x", c));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        long start = convertDTime(20161231235960000L);
        for (int i = 0; i < 24 * 3600 + 5; i++, start += 1000) {
            long dtime = convert2Dtime(start);
            long aaa = convertDTime(dtime);
            System.out.println(dtime + " " + aaa + " " + new Date(aaa));
        }
    }

    /**
     * Use a default value if the provided value is {@code null}.
     * @param <T> the type of value
     * @param value the value to use, but not if {@code null}.
     * @param defaultValue the value to utilize when {@code value} is {@code null}
     * @return {@code value} if not {@code null}, otherwise {@code defaultValue}
     */
    public static <T> T defaultValue(T value, T defaultValue) {
        return (value == null) ? defaultValue : value;
    }


}

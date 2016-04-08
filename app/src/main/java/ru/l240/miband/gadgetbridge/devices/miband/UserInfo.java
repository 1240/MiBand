package ru.l240.miband.gadgetbridge.devices.miband;

import java.util.Arrays;

import ru.l240.miband.gadgetbridge.model.ActivityUser;
import ru.l240.miband.gadgetbridge.service.devices.miband.DeviceInfo;
import ru.l240.miband.gadgetbridge.util.CheckSums;


public class UserInfo {

    private final String btAddress;
    private final String alias;
    private final int gender;
    private final int age;
    private final int height;
    private final int weight;
    private final int type;

    private byte[] data = new byte[20];

    /**
     * Creates a user info with the given data
     *
     * @param address the address of the MI Band to connect to.
     */
    private UserInfo(String address, String alias, int gender, int age, int height, int weight, int type) {
        this.btAddress = address;
        this.alias = alias;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.type = type;
    }

    /**
     * Creates a user info with the given data
     */
    public static UserInfo getDefault(String btAddress) {
        return new UserInfo(btAddress, "1550050550", ActivityUser.defaultUserGender, ActivityUser.defaultUserAge, ActivityUser.defaultUserHeightCm, ActivityUser.defaultUserWeightKg, 0);
    }

    /**
     * Creates a default user info.
     */
    public static UserInfo create(String address, String alias, int gender, int age, int height, int weight, int type) throws IllegalArgumentException {
        if (address == null || address.length() == 0 || alias == null || alias.length() == 0 || gender < 0 || age <= 0 || weight <= 0 || type < 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        try {
            return new UserInfo(address, alias, gender, age, height, weight, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Illegal user info data", ex);
        }
    }

    private int calculateUidFrom(String alias) {
        int uid;
        try {
            uid = Integer.parseInt(alias);
        } catch (NumberFormatException ex) {
            uid = alias.hashCode(); // simple as that
        }
        return uid;
    }

    public byte[] getData(DeviceInfo mDeviceInfo) {
        byte[] sequence = new byte[20];
        int uid = calculateUidFrom(alias);

        sequence[0] = (byte) uid;
        sequence[1] = (byte) (uid >>> 8);
        sequence[2] = (byte) (uid >>> 16);
        sequence[3] = (byte) (uid >>> 24);

        sequence[4] = (byte) (gender & 0xff);
        sequence[5] = (byte) (age & 0xff);
        sequence[6] = (byte) (height & 0xff);
        sequence[7] = (byte) (weight & 0xff);
        sequence[8] = (byte) (type & 0xff);

        int aliasFrom = 9;
        sequence[9] = (byte) (mDeviceInfo.feature & 255); //4
        sequence[10] = (byte) (mDeviceInfo.appearance & 255); //0
        aliasFrom = 11;

        byte[] aliasBytes = alias.substring(0, Math.min(alias.length(), 19 - aliasFrom)).getBytes();
        System.arraycopy(aliasBytes, 0, sequence, aliasFrom, aliasBytes.length);

        byte[] crcSequence = Arrays.copyOf(sequence, 19);
        sequence[19] = (byte) ((CheckSums.getCRC8(crcSequence) ^ Integer.parseInt(this.btAddress.substring(this.btAddress.length() - 2), 16)) & 0xff);

        return sequence;
    }
}

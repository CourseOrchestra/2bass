package ru.curs.bass.ver;

/**
 * Bass version holder.
 *
 * @author Pavel Perminov (packpaul@mail.ru)
 * @since 2019-04-13
 */
public final class BassVersion {

    /**
     * Bass version, f.e. <code>1.1</code>.
     */
    public static final String VERSION;

    static {
        VERSION = BassVersion.class.getPackage().getSpecificationVersion();
    }

    private BassVersion() {
    }

}

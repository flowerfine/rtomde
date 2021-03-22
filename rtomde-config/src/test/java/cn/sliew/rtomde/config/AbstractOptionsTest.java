package cn.sliew.rtomde.config;

import cn.sliew.milky.test.MilkyTestCase;
import org.junit.jupiter.api.Test;

public class AbstractOptionsTest extends MilkyTestCase {

    @Test
    public void testTagName() {
        System.out.println(AbstractOptions.getTagName(this.getClass()));
    }
}

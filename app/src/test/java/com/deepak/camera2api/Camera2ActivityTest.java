package com.deepak.camera2api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
//@Config(shadows={ShadowFoo.class, ShadowBar.class})
//@Config(constants = BuildConfig.class)
public class Camera2ActivityTest {
    private Camera2Activity activity;

    @Before
    public void setUp() throws Exception {
        Camera2Activity activity = Robolectric.setupActivity(Camera2Activity.class);
        activity = Robolectric.buildActivity(Camera2Activity.class)
                .create()
                .resume()
                .get();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertNotNull(activity);
        //ShadowPackageManager packageManager = shadowOf(RuntimeEnvironment.application.getPackageManager());
    }

}

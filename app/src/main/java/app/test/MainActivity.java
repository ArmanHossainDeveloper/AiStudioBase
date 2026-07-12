package app.test;

import android.widget.EditText;
import android.widget.TextView;

import arman.common.infocodes.InfoCode;
import arman.common.ui.DrawerActivity;

public class MainActivity extends DrawerActivity {

    TextView logTV;
    EditText urlEdt;


    @Override
    protected int[] getRequiredPermission() {
        return new int[]{InfoCode.NO_PERMISSION_REQUIRED};
    }
    @Override
    protected void onCreate() {
        setContentView(R.layout.activity_main);
        setRightDrawer(R.layout.drawer_frame, R.layout.menu_drawer);
        initialize();
        onSettingsChange();
    }

    private void initialize() {
        logTV = find(R.id.log_tv);
        urlEdt = find(R.id.url_edt);
    }

    @Override
    protected void onSettingsChange() {
        //enableSwipers(true);
        enableSwipers(preference.getBoolean("enableSwipers", true));
    }

    @Override
    public void onRotate(boolean landscape) {
        if (landscape) toast("Landscape");
        else toast("Portrait");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void onClick(int id) {
        if(id == R.id.exit){
            exit();
        }
    }

}

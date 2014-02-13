package tw.plate.vendor;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener, PlateOrderManager.PlateOrderManagerCallback {

    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    public static PlateOrderManager plateOrderManager;
    private Menu menu;

    private static Timer timer;
    private static TimerTask timerTask;

    //================================================================================
    // Layout Setup
    //================================================================================
    private void layout_setup() {
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);     // turn off display
        actionBar.setDisplayShowTitleEnabled(false);    // turn off display
        /*
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.title_view, null);

        //if you need to customize anything else about the text, do it here.
        //I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
        ((TextView)v.findViewById(R.id.title)).setText(this.getTitle());

        //assign the view to the actionbar
        this.getActionBar().setCustomView(v);
        */


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            //int[] tabsResources= new int[]{R.drawable.tab_location_trans,R.drawable.tab_bill_trans};

            ActionBar.Tab tab = actionBar.newTab();
            TextView customTabView = (TextView)getLayoutInflater().inflate(R.layout.custom_tab, null);
            if (customTabView == null) {
                throw new NullPointerException("custom tab view failed");
            }

            String text = "";
            if (i==0)   text = getString(R.string.tab_cooking);
            else if (i==1)   text = getString(R.string.tab_finish);

            customTabView.setText(text);
            customTabView.setHeight(200);
            tab.setCustomView(customTabView);
            tab.setTabListener(this);
            actionBar.addTab(tab);

            /*
            actionBar.addTab(
                    actionBar.newTab()
                            .setTabListener(this)
                            .setText(text)
                    //.setText(mSectionsPagerAdapter.getPageTitle(i))
                    //.setIcon(getResources().getDrawable(tabsResources[i]))
            );
            */
        }
    }

    // ======= UI Stuff ======= END

    private void setupCookie() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    //================================================================================
    // Runtime Override Events
    //================================================================================
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


        setContentView(R.layout.activity_main);

        // setup system-wide cookie
        setupCookie();

        // login, and first update
        plateOrderManager = new PlateOrderManager(this);
        plateOrderManager.login(this);

        //
        layout_setup();

        // regular refresh
        timerStart();
    }

    /* Timer */
    public void timerStop() {
        timer.cancel();
    }

    public void timerStart() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                refreshData();
            }
        };
        timer.schedule(timerTask, 0, Constants.REFRESH_INT);
    }

    private void refreshData() {
        Log.i(Constants.LOG_TAG, "Main: refresh");
        plateOrderManager.update(this);
        plateOrderManager.updateRestStatus(this);
    }

    // ======================

    // This snippet hides the system bars.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mViewPager.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onBackPressed() {
       ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // FIXME: trying to modify the font size and color for menu item (rest status), but failed
        /*
        LayoutInflater layoutInflater = getLayoutInflater();
        final LayoutInflater.Factory existingFactory = layoutInflater.getFactory();
// use introspection to allow a new Factory to be set
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);

            getLayoutInflater().setFactory(new LayoutInflater.Factory() {
                @Override
                public View onCreateView(String name, Context context,
                                         AttributeSet attrs) {
                    View view = null;
                    // if a factory was already set, we use the returned view
                    if (existingFactory != null) {
                        view = existingFactory.onCreateView(name, context, attrs);
                    }
                    if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")) {
                        try {
                            LayoutInflater f = getLayoutInflater();
                            view = f.createView(name, null, attrs);
                            final View _view = view;
                            new Handler().post(new Runnable() {
                                public void run() {
                                    // set the background drawable
                                    //view.setBackgroundResource(R.drawable.my_ac_menu_background);
                                    ((TextView) _view).setTextColor(Color.YELLOW);
                                    ((TextView) _view).setTextSize(35);
                                }
                            });
                            return view;
                        } catch (InflateException e) {
                            Log.d(Constants.LOG_TAG, "error: " + e.getMessage());
                        } catch (ClassNotFoundException e) {
                            Log.d(Constants.LOG_TAG, "error: " + e.getMessage());
                        }
                    }
                    return null;
                }
            });
        } catch (NoSuchFieldException e) {
            // ...
        } catch (IllegalArgumentException e) {
            // ...
        } catch (IllegalAccessException e) {
            // ...
        }

        return super.onCreateOptionsMenu(menu);
        */
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            simpleVerificationAndSwitchUser();
            return true;
        } else if (id == R.id.action_set_follow_rule) {
            plateOrderManager.post_restaurant_status(Constants.Status.RESTAURANT_STATUS_FOLLOW_OPEN_RULES.ordinal(), this);
            return true;
        } else if (id == R.id.action_set_open) {
            plateOrderManager.post_restaurant_status(Constants.Status.RESTAURANT_STATUS_MANUAL_OPEN.ordinal(), this);
            return true;
        } else if (id == R.id.action_set_close) {
            plateOrderManager.post_restaurant_status(Constants.Status.RESTAURANT_STATUS_MANUAL_CLOSE.ordinal(), this);
            return true;
        } else if (id == R.id.action_rest_status) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void simpleVerificationAndSwitchUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.password_input_title));

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pw = input.getText().toString();
                Log.d(Constants.LOG_TAG, "password >> " + pw);
                if (pw.equals(Constants.SWITCH_USER_PASSWORD)) {
                    switchUser();
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.wrong_password_message),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void switchUser() {
        plateOrderManager.switchUser(this);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final FragmentManager fm;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            //    return PlaceholderFragment.newInstance(position + 1);
            switch(position){
                case 0: return CookingFragment.newInstance();
                case 1: return FinishFragment.newInstance();
                default: return CookingFragment.newInstance();
            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle
                (int position) {
            //Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.tab_cooking);
                case 1:
                    return getString(R.string.tab_finish);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //@SuppressWarnings("ConstantConditions") TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText("This is page: "+Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }


    // Plate Order Manager Callbacks
    @Override
    public void orderUpdated() {
        Log.d(Constants.LOG_TAG, "order updated");

        FragmentManager fm = getSupportFragmentManager();
        String tag;

        tag = makeFragmentName(R.id.pager, 0);
        CookingFragment cookingFragment = (CookingFragment)fm.findFragmentByTag(tag);
        cookingFragment.cookingListUpdate();

        tag = makeFragmentName(R.id.pager, 1);
        FinishFragment finishFragment = (FinishFragment)fm.findFragmentByTag(tag);
        finishFragment.finishListUpdate();

        // see if there's any new order
        ringIfNewOrder();
    }

    @Override
    public void loginCompleted() {
        Log.d(Constants.LOG_TAG, "login completed");
        plateOrderManager.getClosedReason(this);
        plateOrderManager.update(this);
    }

    @Override
    public void statusUpdate() {
        Constants.Status status = plateOrderManager.status;
        boolean is_open = plateOrderManager.is_open;

        MenuItem statusItem = menu.findItem(R.id.action_rest_status);
        if (statusItem == null) return;
        switch (status) {
            case RESTAURANT_STATUS_FOLLOW_OPEN_RULES:
                Log.i(Constants.LOG_TAG, "restaurant follow open rules");
                if (is_open) {
                    statusItem.setTitle(getString(R.string.rest_status_open));
                    statusItem.setIcon(getResources().getDrawable(R.drawable.circle_green));
                } else {
                    statusItem.setTitle(getString(R.string.rest_status_close));
                    statusItem.setIcon(getResources().getDrawable(R.drawable.circle_red));
                }
                break;
            case RESTAURANT_STATUS_MANUAL_CLOSE:
                Log.i(Constants.LOG_TAG, "restaurant close");
                statusItem.setTitle(getString(R.string.rest_status_close) + getString(R.string.rest_status_manual));
                statusItem.setIcon(getResources().getDrawable(R.drawable.circle_red));
                break;
            case RESTAURANT_STATUS_MANUAL_OPEN:
                Log.i(Constants.LOG_TAG, "restaurant open");
                statusItem.setTitle(getString(R.string.rest_status_open) + getString(R.string.rest_status_manual));
                statusItem.setIcon(getResources().getDrawable(R.drawable.circle_green));
                break;
            default:
                Log.i(Constants.LOG_TAG, "know status");
                statusItem.setTitle(getString(R.string.rest_status_error));
                statusItem.setIcon(getResources().getDrawable(R.drawable.circle_yellow));
                break;
        }
    }

    @Override
    public void statusPostCompleted(boolean selectClosedReason) {
        if (selectClosedReason) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.select_manual_closed_reason));

            final List<ClosedReason> closedReasons = ((PlateVendor)getApplication()).closedReasons;
            String crList [] = new String[closedReasons.size()];
            for (int i=0 ; i<closedReasons.size() ; i++) {
                crList[i] = closedReasons.get(i).msg;
            }
            builder.setItems(crList, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int id = closedReasons.get(which).id;
                    Log.d(Constants.LOG_TAG, "closed reason id >> " + id);
                    callPostClosedReason(id);
                }
            });
            builder.show();

        }
    }

    @Override
    public void closedReasonPostSucceed() {
        Toast.makeText(this, getString(R.string.closedtoast_reason_post_succeed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void closedReasonPostFailed() {
        Toast.makeText(this, getString(R.string.closedtoast_reason_post_failed), Toast.LENGTH_LONG).show();
    }

    private void callPostClosedReason(int id) {
        plateOrderManager.postClosedReason(id, this);
    }

    //
    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    /* New Order Ringtone */
    private void ringIfNewOrder() {
        int max_number_slip = ((PlateVendor)getApplication()).max_number_slip;
        boolean newOrderExist = false;
        List<PlateVendorService.OrderSingle> orders = plateOrderManager.orders;
        for (int i=0 ; i<orders.size() ; i++) {
            int ns = orders.get(i).order.pos_slip_number;
            if (ns > max_number_slip) {
                ((PlateVendor)getApplication()).max_number_slip = ns;
                newOrderExist = true;
            }
        }
        // play ringtone
        if (newOrderExist)  playNewOrderRingtone();
    }

    private MediaPlayer mediaPlayer;
    private void playNewOrderRingtone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying())    mediaPlayer.stop();
        mediaPlayer = MediaPlayer.create(this, R.raw.new_order2);
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }


    //
    @Override
    public void networkError() {

        if (((PlateVendor)getApplication()).networkErrorFreezed) {
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_network_error);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.tv_dialog_network_error);
        message.setText(getString(R.string.message_network_error));

        timerStop();

        ((PlateVendor)getApplication()).networkErrorFreezed = true;

        Button retryButton = (Button) dialog.findViewById(R.id.btn_dialog_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.LOG_TAG, "network error" );
                ((PlateVendor)getApplication()).networkErrorFreezed = false;
                timerStart();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //================================================================================
    // End
    //================================================================================
}

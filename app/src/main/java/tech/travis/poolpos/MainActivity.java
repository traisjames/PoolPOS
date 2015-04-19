package tech.travis.poolpos;

/**
 * Created by travis on 2/1/15.
 * Last update by Travis: 4/15/15 3:00
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

/*todo
Flavors are being ignored for now.
 */

public class MainActivity extends Activity {
    private static final String MAIN_PREFS = "";
    private static final String ns = null;
    final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    int mode;
    private int bUID = 0;
    private HashMap<Integer, Integer> ordertracker = new HashMap<Integer, Integer>();  //Key order button ID,  value MM ID
    private HashMap<Integer, Integer> menutracker = new HashMap<Integer, Integer>();  //Key menu button ID,  value MM ID
    private HashMap<Integer, Integer> finaltracker = new HashMap<Integer, Integer>();  //Key menu button ID,  value MM ID
    private HashMap<Integer, Integer> EODtracker = new HashMap<Integer, Integer>();  //Key MMID,  value is count.D
    private ArrayList<MenuMaker> menulist = new ArrayList<MenuMaker>();
    private Calendar c = Calendar.getInstance();

    private PopupWindow popupWindow;

    private boolean doubleBackToExitPressedOnce;
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };
    private Handler mHandler;

    //Added for DEBUGGING
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    //Application events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Started", getMethodName());
        setContentView(R.layout.activity_main);

        openItemsFile();
        createMenuButtons();
        createOrderButtons();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(MAIN_PREFS, 0);

        String EODRestore = settings.getString("SavedEOD", "");
        Log.i("Restoring", EODRestore);


        if (!EODRestore.isEmpty()) {
            String[] EODR = EODRestore.split(",");
            EODtracker.clear();
            for (int i = 0; i < EODR.length; i++) {

                EODtracker.put(i, Integer.parseInt(EODR[i]));

            }
        }

        mode = settings.getInt("Mode", 0);
        setmode();

        Log.i("Finished", getMethodName());
    }

    @Override
    protected void onRestart() {
        Log.i("Started", getMethodName());

        super.onRestart();
        Log.i("Finished", getMethodName());
    }


    @Override
    protected void onPause() {
        Log.i("Started", getMethodName());
        super.onPause();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        //http://developer.android.com/guide/topics/data/data-storage.html
        SharedPreferences settings = getApplicationContext().getSharedPreferences(MAIN_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        //Store settings
        editor.putInt("Mode", mode);


        StringBuilder orderstring = new StringBuilder(EODtracker.size() * 1 + 30);

        for (int i = 0; i < menulist.size(); i++) {

            orderstring.append(EODtracker.get(i) + ",");
        }


        editor.putString("SavedEOD", orderstring.toString());

        // Commit the edits!
        Log.i("Commiting", orderstring.toString());
        Log.i("Commiting", String.valueOf(editor.commit()));
        MenuMaker.itUIDcounter = 0;

        Log.i("Finished", getMethodName());
    }

    @Override
    protected void onStart() {
        Log.i("Started", getMethodName());
        super.onStart();
        Log.i("Finished", getMethodName());
    }

    @Override
    protected void onResume() {
        Log.i("Started", getMethodName());
        super.onResume();
        Log.i("Finished", getMethodName());
    }

    @Override
    protected void onDestroy() {
        Log.i("Started", getMethodName());
        super.onDestroy();


        Log.i("Finished", getMethodName());
    }

    @Override
    protected void onStop() {
        Log.i("Started", getMethodName());
        super.onStop();
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        Log.i("Finished", getMethodName());
    }

    //Require tapping back twice to exit.  THanks to cousin W for idea.
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    //Sets which menu list to use
    private void setmode() {
        View layoutEntry = findViewById(R.id.MenuButtonLayoutEntry);
        View layoutCon = findViewById(R.id.MenuButtonLayoutConsessions);

        if (mode == 0) {
            layoutEntry.setVisibility(VISIBLE);
            layoutCon.setVisibility(GONE);
        } else {
            layoutEntry.setVisibility(GONE);
            layoutCon.setVisibility(VISIBLE);
        }
        Log.i("Finished", getMethodName());
    }

    private void createOrderButtons() {


        Button orderButton;
        int btID;
        // Access LinearLayout element OrderButtonList
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (MenuMaker mm : menulist) {
            orderButton = new Button(this);
            orderButton.setId(bUID++ + 200);
            btID = orderButton.getId();
            orderButton.setText(mm.getCount() + " " + mm.getName() + "s: " + format.format((double) mm.getPrice() / 100));
            ordertracker.put(btID, mm.getUID());

            EODtracker.put(mm.getUID(), 0);
            orderButton.setTypeface(null, Typeface.BOLD);
            orderButton.setTextSize(16);
            orderButton.setVisibility(GONE);
            orderButton.setOnClickListener(new POSOrderListener(btID));
            ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, min(LayoutParams.WRAP_CONTENT, 30), 1);
            linearLayout.addView(orderButton, param);
        }

        Log.i("Finished", getMethodName());
    }


    private void createMenuButtons() {
        ArrayList<MenuMaker> EntryList = new ArrayList<MenuMaker>();
        ArrayList<MenuMaker> ConsessionList = new ArrayList<MenuMaker>();
        LinearLayout layoutVertical;
        for (MenuMaker mm : menulist) {

            if (mm.getType().equals("Entry")) {
                EntryList.add(mm);
            } else {
                ConsessionList.add(mm);
            }
        }


        layoutVertical = (LinearLayout) findViewById(R.id.MenuButtonLayoutEntry);
        genMenuButtons(layoutVertical, EntryList);

        layoutVertical = (LinearLayout) findViewById(R.id.MenuButtonLayoutConsessions);
        genMenuButtons(layoutVertical, ConsessionList);

        Log.i("Finished", getMethodName());
    }

    private void genMenuButtons(LinearLayout layoutVertical, ArrayList<MenuMaker> mml) {

        LinearLayout rowLayout = null;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int FoodSize = mml.size();
        int x = (int) ceil(sqrt(FoodSize));
        int y = (int) ceil(sqrt(FoodSize));
        Button[][] buttons = new Button[y][x];
        int count = x * y + 1;
        int counter;
        int btID;
        String buttontext;

        //create order buttons
        out:
        for (int i = 0; i < y; i++) {
            if (count % x == 1) {
                rowLayout = new LinearLayout(this);
                rowLayout.setWeightSum(x);
                rowLayout.setBaselineAligned(false);
                layoutVertical.addView(rowLayout);
                count = count - x;
            }
            for (int j = 0; j < x; j++) {
                counter = i * x + j;
                if (counter < FoodSize) {
                    buttons[i][j] = new Button(this);

                    buttons[i][j].setId(bUID++ + 100);
                    btID = buttons[i][j].getId();
                    menutracker.put(btID, mml.get(counter).getUID());
                    buttontext = mml.get(counter).getName() + ": " + format.format((double) mml.get(counter).getPrice() / 100);

                    String pattern = "(\\w.+?\\s\\w.+?)\\s(\\w+)";

                    buttons[i][j].setText(buttontext.replaceAll(pattern, "$1\n$2"));
                    buttons[i][j].setEnabled(true);
                    buttons[i][j].setTextSize(14);
                    buttons[i][j].setTypeface(null, Typeface.BOLD);
                    buttons[i][j].setWidth(width / (x + 1) - 30);
                    buttons[i][j].setHeight(max(height / (y + 1), LayoutParams.WRAP_CONTENT));
                    buttons[i][j].setOnClickListener(new POSListListener(counter));
                    rowLayout.addView(buttons[i][j]);

                } else {
                    break out;
                }
            }
        }
        Log.i("Finished", getMethodName());
    }

    public int updatetotal() {

        int total = 0;
        double dtotal;
        TextView txt = (TextView) findViewById(R.id.txtPrice);
        for (MenuMaker mmi : menulist) {
            total += mmi.getCount() * mmi.getPrice();
        }
        dtotal = total;

        txt.setText(format.format(dtotal / 100));
        refreshOrder();
        Log.i("Finished", getMethodName());
        return total;
    }

    private void refreshOrder() {
        LinearLayout OrderView = (LinearLayout) findViewById(R.id.OrderButtonList);
        int oID;
        for (int i = 0; i < OrderView.getChildCount(); i++) {
            Button child = (Button) OrderView.getChildAt(i);

            if (menulist.get(i).getCount() > 0) {

                oID = ordertracker.get(child.getId());
                child.setText(menulist.get(oID).getCount() + " " + menulist.get(oID).getName() + "s: " + format.format((double) menulist.get(oID).getPrice() / 100));

                child.setVisibility(VISIBLE);
            } else {
                child.setVisibility(GONE);
            }
            child.invalidate();
        }
        Log.i("Finished", getMethodName());
    }


    public void submitorder() {

        updatetotal();

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.orderup, null);
        popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        popupWindow.setWidth(width);
        popupWindow.setHeight(height);


        Button orderButton;
        int btID;
        // Access LinearLayout element OrderButtonList


        LinearLayout linearLayout = (LinearLayout) popupView.findViewById(R.id.FinalOrder);
        //linearLayout.setOrientation(LinearLayout.VERTICAL);
        boolean entryonly = true;
        for (MenuMaker mm : menulist) {
            EODtracker.put(mm.getUID(), EODtracker.get(mm.getUID()) + mm.getCount());

            if (mm.getCount() > 0 && !mm.getType().equals("Entry")) {
                orderButton = new Button(this);
                orderButton.setId(bUID++ + 600);
                btID = orderButton.getId();
                orderButton.setText(mm.getName() + ": " + mm.getCount());
                finaltracker.put(btID, mm.getUID());


                orderButton.setHeight(20);
                orderButton.setWidth(200);
                orderButton.setTextSize(14);
                orderButton.setVisibility(VISIBLE);
                orderButton.setOnClickListener(new POSFinalOrderListener(btID));
                LayoutParams param = new LinearLayout.LayoutParams(200, LayoutParams.WRAP_CONTENT, 0);
                linearLayout.addView(orderButton, param);
                entryonly = false;


            }
        }
        //only shows window if something from conessions was added.
        storeorder();
        if (entryonly) {
            clearorder();
        } else {
            popupWindow.showAsDropDown(findViewById(R.id.posMain), 0, -1 * (height));

        }


        Log.i("Finished", getMethodName());
    }

    private void storeorder() {
        StringBuilder orderstring = new StringBuilder(menulist.size() * 1 + 30);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        orderstring.append(formattedDate + ",");
        for (MenuMaker mm : menulist) {
            orderstring.append(mm.getCount() + ",");
        }
        Log.v("Order:", orderstring.toString());
        saveCSV("Orders.csv", orderstring.toString());
        Log.i("Finished", getMethodName());
    }


    public void clearorder() {
        for (int i = 0; i < menulist.size(); i++) {
            menulist.get(i).resetCount();
        }

        updatetotal();
        Log.i("Finished", getMethodName());
    }


    //XML created onclick listeners
    public void oCclearorder(View v) {
        clearorder();
        Log.i("Finished", getMethodName());
    }

    public void oCsubmitorder(View v) {
        submitorder();
        Log.i("Finished", getMethodName());
    }

    public void oCConessionMode(View v) {
        mode = 1;
        setmode();
        Log.i("Finished", getMethodName());
    }

    public void oCEntryMode(View v) {
        mode = 0;
        setmode();

        Log.i("Finished", getMethodName());
    }

    public void oCEOS(View v) {
        EOS();

        Log.i("Finished", getMethodName());
    }

    public void ocEODFinalize(View v) {

        StringBuilder orderstring = new StringBuilder(menulist.size() * 1 + 30);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());

        orderstring.append(formattedDate + ",");

        for (int i = 0; i < menulist.size(); i++) {

            orderstring.append(EODtracker.get(i) + ",");
            //Reset EODtracker to 0
            EODtracker.put(i, 0);
        }

        Log.v("Daily:", orderstring.toString());
        saveCSV("Daily.csv", orderstring.toString());


        popupWindow.dismiss();
        Log.i("Finished", getMethodName());
    }

    public void ocEODBack(View v) {
        popupWindow.dismiss();
        Log.i("Finished", getMethodName());
    }

    private void EOS() {

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.eoddisplay, null);
        popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        popupWindow.setWidth(width);
        popupWindow.setHeight(height);
        int Daytotal = 0;


        Button orderButton;
        int btID;
        // Access LinearLayout element OrderButtonList


        LinearLayout linearLayout = (LinearLayout) popupView.findViewById(R.id.llEODList);
        //linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < menulist.size(); i++) {

            orderButton = new Button(this);
            btID = orderButton.getId();

            orderButton.setText(menulist.get(i).getName() + ": " + EODtracker.get(i));
            Daytotal = Daytotal + menulist.get(i).getPrice() * EODtracker.get(i);
            orderButton.setWidth(200);
            orderButton.setTextSize(12);
            orderButton.setVisibility(VISIBLE);
            LayoutParams param = new LinearLayout.LayoutParams(200, LayoutParams.WRAP_CONTENT, 0);
            linearLayout.addView(orderButton, param);


        }
        //WTF

        TextView txt = (TextView) popupView.findViewById(R.id.EODTotal);

        txt.setText(format.format(Daytotal / 100));

        popupWindow.showAsDropDown(findViewById(R.id.posMain), 0, -1 * (height));

        //saveCSV("EOD.csv",output);
        Log.i("Finished", getMethodName());
    }

    //File handleing
private void openItemsFile() {

    InputStream in = null;
    File file = null;
    if (isExternalStorageReadable()) {
        //Find the directory for the SD Card using the API
        //*Don't* hardcode "/sdcard"
        File sdcard = Environment.getExternalStorageDirectory();

        //Get the text file
        file = new File(sdcard, "items.xml");

        }

    try {
        if (file.exists()) //check if xml is on sd card
        {
            //it does so use it
            in = new BufferedInputStream(new FileInputStream(file));
        } else {
            //nope
            in = getAssets().open("items.xml");
        }

        parse(in);

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (XmlPullParserException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            if (in != null) {
                in.close();
                }
        } catch (IOException e) {
            e.printStackTrace();
            }
    }

    Log.i("Finished", getMethodName());
    }

    private void saveItemsFile() {
    }

    private void saveCSV(String filename, String output) {

        if (isExternalStorageWritable()) {
            try {
                File sdcard = Environment.getExternalStorageDirectory();
                if (!sdcard.exists()) {
                    Log.i("File", "Creating New");
                    sdcard.createNewFile();
                }
                File gpxfile = new File(sdcard, filename);
                FileWriter writer = new FileWriter(gpxfile, true);
                writer.append(output + "\n");
                writer.flush();
                writer.close();
                Log.i("File", "Write complete");
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        Log.i("Finished", getMethodName());

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //XML parsing
    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(START_TAG, ns, "Document");
        while (parser.next() != END_TAG) {
            if (parser.getEventType() != START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Item")) {
                menulist.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private MenuMaker readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(START_TAG, ns, "Item");

        String itName = null;
        int itPrice = 0;
        String itType = null;
        String itFlavor = "";
        String tmpFlavor = "";

        while (parser.next() != END_TAG) {
            if (parser.getEventType() != START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Item_Name")) {
                itName = readName(parser);
            } else if (name.equals("Price")) {
                itPrice = readPrice(parser);
            } else if (name.equals("Type")) {
                itType = readType(parser);
            } else if (name.equals("Flavor")) {
                tmpFlavor = readFlavors(parser);
                if (!tmpFlavor.isEmpty()) {
                    itFlavor = tmpFlavor + "," + itFlavor;
                }
            } else {
                skip(parser);
            }
        }
        return new MenuMaker(itName, itPrice, itFlavor, itType);
    }

    // Processes Item_Name tags in the feed.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(START_TAG, ns, "Item_Name");
        String text = readText(parser);
        parser.require(END_TAG, ns, "Item_Name");
        return text;
    }

    // Processes Price tags in the feed.
    private int readPrice(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(START_TAG, ns, "Price");
        String text = readText(parser);
        parser.require(END_TAG, ns, "Price");
        return Integer.parseInt(text);
    }

    // Processes Type tags in the feed.
    private String readType(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(START_TAG, ns, "Type");
        String text = readText(parser);
        parser.require(END_TAG, ns, "Type");
        return text;
    }

    // Processes Flavor tags in the feed.
    private String readFlavors(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(START_TAG, ns, "Flavor");
        String text = readText(parser);
        parser.require(END_TAG, ns, "Flavor");
        return text;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case END_TAG:
                    depth--;
                    break;
                case START_TAG:
                    depth++;
                    break;
            }
        }
    }

    //Programicly created onclick listeners

    //Menu button click listener.  Runs when an item is selected to be ordered
    class POSListListener implements View.OnClickListener {
        int buttonID;

        public POSListListener(int id) {
            buttonID = id;
        }

        @Override
        public void onClick(View v) {
            //find menulist with menu ID of buttonID
            int vid = v.getId();

            menulist.get(menutracker.get(vid)).incCount();

            updatetotal();

            Log.i("Finished", getMethodName());
        }
    }

    //Remove Order itemscsv
    class POSOrderListener implements View.OnClickListener {

        int buttonID;

        public POSOrderListener(int id) {
            buttonID = id;
        }


        @Override
        public void onClick(View view) {
            Log.v("Removing", "Button: " + buttonID);

            menulist.get(ordertracker.get(buttonID)).decCount();


            updatetotal();
            Log.i("Finished", getMethodName());
        }

    }

    //on order up, clicking a button to mark it as gotten.
    class POSFinalOrderListener implements View.OnClickListener {

        int buttonID;

        public POSFinalOrderListener(int id) {
            buttonID = id;
        }


        @Override
        public void onClick(View view) {
            int oID;
            Log.v("Removing", "Button: " + buttonID);

            menulist.get(finaltracker.get(buttonID)).decCount();

            oID = finaltracker.get(view.getId());
            Button bview = (Button) view;
            bview.setText(menulist.get(oID).getName() + ": " + menulist.get(oID).getCount());

            view.invalidate();
            ViewGroup layout = (ViewGroup) view.getParent();
            if (menulist.get(finaltracker.get(buttonID)).getCount() == 0) {

                if (null != layout) {
                    layout.removeView(view);
                }
            }
            Log.v("Child count", layout.getChildCount() + "");
            //May be sinning here but it works:
            if (layout.getChildCount() == 0) {

                popupWindow.dismiss(); //Dismiss PopupWindow
                finaltracker.clear();
                clearorder();
                refreshOrder();


            }

            Log.i("Finished", getMethodName());
        }

    }

    //add new item method
    class ButtonAddNewItem implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String itemName = "";
            int itemPrice = 0;
            String itemFlavors = "";
            //orderlist.add(new MenuMaker(itemName, itemPrice, itemFlavors));
            Log.i("Finished", getMethodName());
        }

    }

}
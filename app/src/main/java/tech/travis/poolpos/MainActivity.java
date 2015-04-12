package tech.travis.poolpos;

/**
 * Created by travis on 2/1/15.
 * Last update by Travis: 4/12/15 3:00
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

/*todo
Flavors are being ignored for now.
 */

public class MainActivity extends Activity {
    private static final String MAIN_PREFS = "MainPrefs";
    private static final String STORED_ORDERES = "OrderedPrefs";

    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    HashMap<Integer, Integer> ordertracker = new HashMap<Integer, Integer>();  //Key is the item's UID,  value is count
    ArrayList<MenuMaker> menulist = new ArrayList<MenuMaker>();
    int mode;
    private static final String ns = null;
    private static int bUID = 0;


    //Added for DEBUGGING
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    //Application events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        openItemsFile();
        createMenuButtons();
        createOrderButtons();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(MAIN_PREFS, 0);

        mode = settings.getInt("Mode", 0);
        setmode();

        Log.i("Finished", getMethodName());
    }

    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        //http://developer.android.com/guide/topics/data/data-storage.html
        SharedPreferences settings = getSharedPreferences(MAIN_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();

        //Store settings
        editor.putInt("Mode", mode);

        // Commit the edits!
        editor.commit();
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
    }

    private void createOrderButtons() {

        //we need to check to see if this item exits, and if it does incriment item count.
        Button buttons;
        int btID;
        // Access LinearLayout element OrderButtonList
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (MenuMaker mm : menulist) {
            buttons = new Button(this);
            buttons.setId(bUID++ + 200);
            btID = buttons.getId();
            buttons.setText(mm.getName() + ": " + format.format((double) mm.getPrice() / 100 * mm.getCount()) + "::" + btID);
            menulist.get(mm.getUID()).setOrderButton(btID);
            buttons.setTop(5);
            buttons.setHeight(20);
            buttons.setTextSize(12);
            buttons.setVisibility(GONE);
            buttons.setOnClickListener(new POSOrderListener(btID));
            ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1);
            linearLayout.addView(buttons, param);
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
        LayoutGen(layoutVertical, EntryList);

        layoutVertical = (LinearLayout) findViewById(R.id.MenuButtonLayoutConsessions);
        LayoutGen(layoutVertical, ConsessionList);

        Log.i("Finished", getMethodName());
    }

    private void LayoutGen(LinearLayout layoutVertical, ArrayList<MenuMaker> mml) {
        //todo limit to 4 accross
        LinearLayout rowLayout = null;
        int FoodSize = mml.size();
        int x = (int) ceil(sqrt(FoodSize));
        int y = (int) ceil(sqrt(FoodSize));
        Button[][] buttons = new Button[y][x];
        int count = x * y + 1;
        int counter;

        int btID;

        //create buttons
        out:
        for (int i = 0; i < y; i++) {
            if (count % x == 1) {
                rowLayout = new LinearLayout(this);
                rowLayout.setWeightSum(x);
                layoutVertical.addView(rowLayout);
                count = count - x;
            }
            for (int j = 0; j < x; j++) {
                counter = i * x + j;
                if (counter < FoodSize) {
                buttons[i][j] = new Button(this);

                    buttons[i][j].setId(bUID++ + 100);
                    btID = buttons[i][j].getId();
                    menulist.get(mml.get(counter).getUID()).setOrderButton(btID);
                    buttons[i][j].setText(mml.get(counter).getName() + ": " + format.format((double) mml.get(counter).getPrice() / 100) + "::" + buttons[i][j].getId());
                    buttons[i][j].setEnabled(true);
                buttons[i][j].setTextSize(12);
                    buttons[i][j].setOnClickListener(new POSListListener(counter));
                    //buttons[i][j].setWidth(150);


                rowLayout.addView(buttons[i][j]);


                } else {
                    break out;
                }


            }
        }


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
        Log.i("Finished", getMethodName());
        return total;
    }


    /* Todo
    * creates button and tosses to add to order.  No icrementing reused itemscsv.*/
    int addtoordermenu(int buttonID) {

        //we need to check to see if this item exits, and if it does incriment item count.

        // Access LinearLayout element OrderButtonList
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.OrderButtonList);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //button ID is list menu, working ID is button
        int workingID = buttonID;

        MenuMaker mm;
        mm = new MenuMaker(menulist.get(workingID).getName(), menulist.get(workingID).getPrice(), "", 1, menulist.get(workingID).getType());


        ViewGroup.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1);

        //Create Buttons test

        Button buttons = new Button(this);
        //Set button ID
        //todo


        //set text in buttons
        buttons.setText(menulist.get(workingID).getName() + ": " + format.format((double) menulist.get(workingID).getPrice() / 100 * menulist.get(workingID).getCount()) + "::" + buttons.getId());

        buttons.setTop(5);
        buttons.setHeight(20);
        buttons.setTextSize(12);
        buttons.setOnClickListener(new POSOrderListener(workingID));

        linearLayout.addView(buttons, param);
        Log.i("Finished", getMethodName());


        return mm.getUID();

    }

    public void submitorder() {
        String finalorder = "";
        //Working Here
        Log.i("Order", finalorder);

        updatetotal();
        clearorder();
        Log.i("Finished", getMethodName());
    }

    public void clearorder() {
        //Todo


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
        clearorder();
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
        //createMenuButtons();

        Log.i("Finished", getMethodName());
    }

    public void oCEOS(View v) {
        Log.i("Finished", getMethodName());
    }

    //Programicly created onclick listeners
    /* Menu button click listener.  Runs when an item is selected to be ordered
     *  */
    class POSListListener implements View.OnClickListener {
        int buttonID;

        public POSListListener(int id) {
            buttonID = id;
        }

        @Override
        public void onClick(View v) {
            int UID = 0;

            //find menulist with menu ID of buttonID
            for (MenuMaker mmi : menulist) {
                //compare ID
                if (mmi.getMenuButton() == buttonID) {
                    UID = mmi.getUID();

                    break;
                }
            }

            //get UID from orderid
            menulist.get(UID).incCount();

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

        public int getButtonID() {
            return buttonID;
        }


        @Override
        public void onClick(View view) {
            Log.v("Removing", "Button: " + buttonID);
            //todo check the list of already created buttons and if exists increase count.

            ViewGroup parentView = (ViewGroup) view.getParent();
            parentView.removeView(view);


            updatetotal();
            Log.i("Finished", getMethodName());
        }

    }

    //remove item method
    //find all based on price
    //add item from menu to order.

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


    //File handleing
    private void openItemsFile() {
        List lst = new ArrayList();
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

            lst = parse(in);

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


    }

    private void saveItemsFile() {
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

}
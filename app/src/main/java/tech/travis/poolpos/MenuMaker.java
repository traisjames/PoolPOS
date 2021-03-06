package tech.travis.poolpos;

/**
 * Created by travis on 3/26/15.
 */


class MenuMaker {
    static int itUIDcounter = 0;
    /**
     * Make an object with 3 properties, item Name, item Price, item flavors.  Also include UID hash
     * Public side much allow return of item Name, price, and possible flavors when given a UID, name or price
     * Prices are using int to save memory and keep presision.  Price of item = itPrice /100
     */
    private String itName = "";
    private int itPrice = 0;
    private String[] itFlavors = {};
    private int itUID = 0;
    private int itCount = 0;
    private String itType = "NA";
    private int itColor = 0;


    //constructor
    public MenuMaker() {
        this.itName = "";
        this.itPrice = 0;
        this.itFlavors = null;
        this.itCount = 0;
        this.itType = "NA";
        this.itUID = itUIDcounter++;
        this.itColor = 0;
    }


    public MenuMaker(String itemname, int itemprice, String itemFlavors, String itemtype, int itemcolor) {

        itName = itemname;
        itPrice = itemprice;
        itFlavors = parseFlavors(itemFlavors);
        itType = itemtype;
        this.itCount = 0;
        itUID = itUIDcounter++;
        itColor = itemcolor;

    }


    public int getItColor() {
        return itColor;
    }

    public String getName() {
        return itName;
    }

    public int getUID() {
        return itUID;
    }

    public int getPrice() {
        return itPrice;
    }

    public String getType() {
        return itType;
    }


    public String[] getFlavors() {
        return itFlavors;
    }

    public int incCount() {
        itCount++;
        return itCount;
    }

    public int decCount() {
        if (itCount > 0) {
            itCount--;
        }
        return itCount;
    }

    public int getCount() {
        return itCount;
    }

    public void resetCount() {
        itCount = 0;
    }

    public int getTotalPrice() {
        return itCount * itPrice;
    }


    private String[] parseFlavors(String Flavors) {
        if (Flavors.isEmpty()) {
            return null;
        } else {
            return Flavors.split(",");
        }
    }

}
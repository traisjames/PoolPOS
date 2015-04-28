package tech.travis.poolpos;


import android.app.ProgressDialog;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

/**
 * Created by travis on 4/18/15.
 */
/*
 Retreve from ftp://tgftp.nws.noaa.gov/data/observations/metar/stations/KDEH.TXT:
 2015/04/19 03:55
 KDEH 190355Z AUTO 16005KT 10SM BKN060 OVC070 16/03 A2994 RMK AO2

 And return:

 Location: KDEH
 Day of month: 19
 Time: 03:55 UTC
 Report is fully automated, with no human intervention or oversight
 Wind: True direction = 160 degrees, Speed: 5 knots
 Visibility: 10 Statute Miles
 Clouds: Broken sky , at 6000 feet above aerodrome level
 Clouds: Overcast sky , at 7000 feet above aerodrome level
 Temperature: 16 degrees Celsius
 Dewpoint: 03 degrees Celsius
 QNH: 29.94 inHg


//todo
Retreve from https://alerts.weather.gov/cap/wwaatmget.php?x=MNC055&y=0 (XML file)
watchs and warnings.

 */



class WeatherParse {
    public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
    public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
    public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
    public static final int MESSAGE_CONNECTING_STARTED = 1004;
    public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;
    private static final String ns = null;
    private int temp;
    private int dp;
    private int cloudc;
    private int pressure;
    private int windd;
    private int YEAR;
    private int DAY;
    private int MONTH;
    private int MIN;
    private int HOUR;
    private double winds, windg;
    private String weatherstatus = "", station;
    private String url = "";
    // instance variables
    private MainActivity thisActivity;
    private Thread downloaderThread;
    private ProgressDialog progressDialog;

    //Added for DEBUGGING
    private static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getFileName() + " " + Thread.currentThread().getStackTrace()[3].getMethodName() + " at " + Thread.currentThread().getStackTrace()[3].getLineNumber();

    }

    public int getTempC() {
        return temp;
    }

    public int getTempF() {
        return (int) (temp * 1.8 + 32);
    }

    public int getDp() {
        return dp;
    }

    public int getCloudc() {
        return cloudc;
    }

    public String getClouds() {
        if (cloudc < 5) {
            return "clear";
        } else if (cloudc < 50) {
            return "partly cloudy";
        } else if (cloudc < 95) {
            return "mostly cloudy";
        } else if (cloudc <= 100) {
            return "overcast";
        } else {
            return "unknown";
        }
    }

    public int getPressure() {
        return pressure;
    }

    public int getWindd() {
        return windd;
    }

    public int getYEAR() {
        return YEAR;
    }

    public int getDAY() {
        return DAY;
    }

    public int getMONTH() {
        return MONTH;
    }

    public int getMIN() {
        return MIN;
    }

    public int getHOUR() {
        return HOUR;
    }

    public double getWinds() {
        return winds;
    }

    public double getWindg() {
        return windg;
    }

    public String getWeatherstatus() {
        return weatherstatus;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public Calendar getTimeDate() {
        Calendar c = Calendar.getInstance();
        Integer i = c.getTimeZone().getDSTSavings();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(YEAR, MONTH, DAY, HOUR + i, MIN);
        return c;
    }


    public boolean getweather(String stationID) {


        //Log.i("Starting", getMethodName());
        String metarstring;
        String ftpsite = "http://weather.noaa.gov/pub/data/observations/metar/stations/";
        String weird = ftpsite + stationID.toUpperCase() + ".TXT";
        DownloaderThread downloaderThread = new DownloaderThread(thisActivity, weird);
        downloaderThread.start();

        while (downloaderThread.getTxtfile().equals("")) {
            try {
                //Log.i("Waiting", getMethodName());
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //Log.e("Wait Fail", getMethodName());
                e.printStackTrace();
            }
        }
        metarstring = downloaderThread.getTxtfile();
        if (!metarstring.isEmpty()) {
            metar_decode(metarstring);


            //Log.i("Finished", getMethodName());
            return true;
        } else {
            return false;
        }
    }

    private boolean is_num_digit(char ch) {
        return ((ch == '0') || (ch == '1') || (ch == '2') || (ch == '3') ||
                (ch == '4') || (ch == '5') || (ch == '6') || (ch == '7') ||
                (ch == '8') || (ch == '9'));
    }

    private boolean is_alphabetic_char(char ch) {
        return ((ch >= 'A') && (ch <= 'Z'));
    }

    private void decode_token(String token) {
        StringBuilder add_output = new StringBuilder();


        // Check if token is Wind indication
        Pattern reWindKT = Pattern.compile("^(\\d{3}|VRB)(\\d{2,3})(G\\d{2,3})?(KT|MPS|KMH)$");
        Matcher mreWindKT = reWindKT.matcher(token);
        if (mreWindKT.matches()) {

            String swindd = mreWindKT.group(1);
            String swinds = mreWindKT.group(2);
            String swindg = mreWindKT.group(3);
            String swindu = mreWindKT.group(4);

            if (swindd.equals("VRB")) {
                windd = 999;
            } else {
                windd = Integer.parseInt(swindd);
            }

            if ((swindg == null) || (swindg.length() == 0)) {
                windg = 0;
            } else {
                windg = Integer.parseInt(swindg.substring(1));
            }

            if (swindu.equals("KT")) {
                winds = Integer.parseInt(swinds) * 1.15077945;
                windg = windg * 1.15077945;
            } else if (swindu.equals("KMH")) {
                winds = Integer.parseInt(swinds) * 0.621371;
                windg = windg * 0.621371;
            } else {
                winds = Integer.parseInt(swinds);
                windg = Integer.parseInt(swindg);
            }


            /**
             // Wind token: dddss(s){Gss(s)}KT -- ddd is true direction, ss(s) speed in knots
             myArray = reWindKT.exec(token);
             String units = myArray[4];
             add_output.append("Wind: ");
             if(myArray[1].equals("VRB"))
             add_output.append(" Variable in direction");
             else
             add_output.append(" True direction = " + myArray[1] + " degrees");
             add_output.append(", Speed: " + Integer.parseInt(myArray[2],10));
             if(units.equals("KT")) add_output.append(" knots");
             else if(units.equals("KMH")) add_output.append(" km/h");
             else if(units.equals("MPS")) add_output.append(" m/s");
             if(myArray[3] != null)
             {
             // I don't have the time nor the energy to investigate why
             // MSIE and Firefox behave differently with respect to an
             // omitted regular subexpression. Hence this quick hack to
             // detect if myArray[3] is not a number.
             if (myArray[3]!="")
             {
             add_output.append(", with Gusts of maximum speed " + Integer.parseInt(myArray[3].substring(1,myArray[3].length),10));
             if(units.equals("KT")) add_output.append(" knots");
             else if(units.equals("KMH")) add_output.append(" km/h");
             else if(units.equals("MPS")) add_output.append(" m/s");
             }
             }

             add_output.append("\\n"); return add_output.toString();*/
            return;
        }

        /**
         // Check if token is "variable wind direction"
         Pattern reVariableWind = Pattern.compile("^(\\d{3})V(\\d{3})$");
         Matcher mreVariableWind = reVariableWind.matcher(token);
         if(mreVariableWind.matches())
         {
         // Variable wind direction: aaaVbbb, aaa and bbb are directions in clockwise order
         add_output.append("Wind direction is variable between "+token.substring(0,3)+" and "+token.substring(4,3)+"\\n");
         return add_output.toString();
         }*/


        /** // Check if token is visibility
         Pattern reVis = Pattern.compile("^(\\d{4})(N|S)?(E|W)?$");
         Matcher mreVis = reVis.matcher(token);
         if(mreVis.matches())
         {
         //String myArray = reVis.exec(token);
         add_output.append("Visibility: ");
         if(myArray[1].equals("9999"))
         add_output.append("10 km or more");
         else if (myArray[1].equals("0000"))
         add_output.append("less than 50 m");
         else
         add_output.append(Integer.parseInt(myArray[1],10) + " m");

         String dir = "";
         if(!myArray[2].isEmpty())
         {
         dir=dir + myArray[2];
         }
         if(!myArray[3].isEmpty())
         {
         dir=dir + myArray[3];
         }
         if(dir != "")
         {
         add_output.append(" direction ");
         if(dir.equals("N")) add_output.append("North");
         else if(dir.equals("NE")) add_output.append("North East");
         else if(dir.equals("E")) add_output.append("East");
         else if(dir.equals("SE")) add_output.append("South East");
         else if(dir.equals("S")) add_output.append("South");
         else if(dir.equals("SW")) add_output.append("South West");
         else if(dir.equals("W")) add_output.append("West");
         else if(dir.equals("NW")) add_output.append("North West");
         }
         add_output.append("\\n"); return add_output.toString();
         }*/

        /** // Check if token is Statute-Miles visibility
         Pattern reVisUS = Pattern.compile("(SM)$");
         Matcher mreVisUS = reVisUS.matcher(token);
         if(mreVisUS.matches())
         {
         add_output.append("Visibility: ");
         String myVisArray[] = token.split("S");
         add_output.append(myVisArray[0]);
         add_output.append(" Statute Miles\\n");
         }
         */

        // Check if token is QNH indication in mmHg or hPa
        Pattern reQNHhPa = Pattern.compile("Q(\\d{3,4})");
        Matcher mreQNHhPa = reQNHhPa.matcher(token);
        if (mreQNHhPa.matches()) {
            // QNH token: Qpppp -- pppp is pressure hPa
            //"QNH (Sea-level pressure)
            pressure = (int) (Integer.parseInt(mreQNHhPa.group(1)) * 0.0295333727 * 100);
            return;
        }

        // Check if token is QNH indication in mmHg: Annnn
        Pattern reINHg = Pattern.compile("A(\\d{4})");
        Matcher mreINHg = reINHg.matcher(token);
        if (mreINHg.matches()) {
            //"QNH: "
            pressure = Integer.parseInt(mreINHg.group(1));
            return;
        }

        /**
         // Check if token is runway visual range (RVR) indication
         Pattern reRVR = Pattern.compile("^R(\\d{2})(R|C|L)?\\/(M|P)?(\\d{4})(V\\d{4})?(U|D|N)?$");
         Matcher mreRVR = reRVR.matcher(token);
         if(mreRVR.matches())
         {
         //String myArray = reRVR.exec(token);
         add_output.append("Runway ");
         add_output.append(myArray[1]);
         if(!myArray[2].isEmpty())
         {
         if(myArray[2].equals("L")) add_output.append(" Left");
         else if(myArray[2].equals("R")) add_output.append(" Right");
         else if(myArray[2].equals("C")) add_output.append(" Central");
         }
         add_output.append(", touchdown zone visual range is ");
         if(!myArray[5].isEmpty())
         {
         // Variable range
         add_output.append("variable from a minimum of ");
         if(myArray[3].equals("P")) add_output.append("more than ");
         else if(myArray[3].equals("M")) add_output.append("less than ");
         add_output.append(myArray[4]);
         add_output.append(" meters");
         add_output.append(" until a maximum of "+myArray[5].substring(1,myArray[5].length())+" meters");
         }
         else
         {
         // Single value
         if( (!myArray[3].isEmpty()) &&
         (!myArray[4].isEmpty()) )
         {
         if(myArray[3].equals("P")) add_output.append("more than ");
         else if(myArray[3].equals("M")) add_output.append("less than ");
         add_output.append(myArray[4]);
         add_output.append(" meters");
         }
         }
         if( (myArray.length > 5) && (!myArray[6].isEmpty()) )
         {
         if(myArray[6].equals("U")) add_output.append(", and increasing");
         else if(myArray[6].equals("D")) add_output.append(", and decreasing");
         }
         add_output.append("\\n");
         return add_output.toString();
         }*/


        /** // Check if token is CAVOK
         if(token.equals("CAVOK"))
         {
         add_output.append("CAVOK conditions: Visibility 10 km or more,\\n no cloud below 5.000 feet or below the MSA (whichever is greater), \\n no cumulonimbus, and no significant weather fenomena in\\n the aerodrome or its vicinity\\n");
         return add_output.toString();
         }


         // Check if token is NOSIG
         if(token.equals("NOSIG"))
         {
         add_output.append("No significant changes expected in the near future\\n");
         return add_output.toString();
         }*/


        // Check if token is a present weather code - The regular expression is a bit
        // long, because several precipitation types can be joined in a token, and I
        // don't see a better way to get all the codes.
        Pattern reWX = Pattern.compile("^(\\-|\\+)?(VC)?(MI|BC|BL|SH|TS|FZ|PR)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS|BR|FG|FU|VA|DU|SA|HZ|PO|SQ|FC|SS|DS)$");
        Matcher mreWX = reWX.matcher(token);
        if (mreWX.matches()) {
            while (mreWX.find()) {
                if (mreWX.group().equals("-")) {
                    add_output.append("Light ");
                }
                if (mreWX.group().equals("+")) {
                    add_output.append("Strong ");
                }
                if (mreWX.group().equals("VC")) {
                    add_output.append("In the vicinity, ");
                }
                if (mreWX.group().equals("MI")) {
                    add_output.append("Shallow ");
                }
                if (mreWX.group().equals("BC")) {
                    add_output.append("Patches of ");
                }
                if (mreWX.group().equals("SH")) {
                    add_output.append("Showers of ");
                }
                if (mreWX.group().equals("TS")) {
                    add_output.append("Thunderstorms ");
                }
                if (mreWX.group().equals("FZ")) {
                    add_output.append("Freezing (or super-cooled) ");
                }
                if (mreWX.group().equals("PR")) {
                    add_output.append("Partial ");
                }
                if (mreWX.group().equals("DZ")) {
                    add_output.append("Drizzle ");
                }
                if (mreWX.group().equals("RA")) {
                    add_output.append("Rain ");
                }
                if (mreWX.group().equals("SN")) {
                    add_output.append("Snow ");
                }
                if (mreWX.group().equals("SG")) {
                    add_output.append("Snow grains ");
                }
                if (mreWX.group().equals("IC")) {
                    add_output.append("Ice Crystals ");
                }
                if (mreWX.group().equals("PL")) {
                    add_output.append("Ice Pellets ");
                }
                if (mreWX.group().equals("GR")) {
                    add_output.append("Hail ");
                }
                if (mreWX.group().equals("GS")) {
                    add_output.append("Small hail (< 5 mm diameter) and/or snow pellets ");
                }
                if (mreWX.group().equals("BR")) {
                    add_output.append("Mist ");
                }
                if (mreWX.group().equals("FG")) {
                    add_output.append("Fog ");
                }
                if (mreWX.group().equals("FU")) {
                    add_output.append("Smoke ");
                }
                if (mreWX.group().equals("VA")) {
                    add_output.append("Volcanic Ash ");
                }
                if (mreWX.group().equals("DU")) {
                    add_output.append("Widespread dust ");
                }
                if (mreWX.group().equals("SA")) {
                    add_output.append("Sand ");
                }
                if (mreWX.group().equals("HZ")) {
                    add_output.append("Haze ");
                }
                if (mreWX.group().equals("PO")) {
                    add_output.append("Dust/Sand whirls ");
                }
                if (mreWX.group().equals("SQ")) {
                    add_output.append("Squall ");
                }
                if (mreWX.group().equals("FC")) {
                    add_output.append("Funnel clouds ");
                }
                if (mreWX.group().equals("SS")) {
                    add_output.append("Sandstorm ");
                }
                if (mreWX.group().equals("DS")) {
                    add_output.append("Duststorm ");
                }
            }

            weatherstatus = add_output.toString();
            return;
        }


 /*// Check if token is recent weather observation
 Pattern reREWX = Pattern.compile("^RE(\\-|\\+)?(VC)?(MI|BC|BL|SH|TS|FZ|PR)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS)?(DZ|RA|SN|SG|IC|PL|GR|GS|BR|FG|FU|VA|DU|SA|HZ|PO|SQ|FC|SS|DS)?$");
 Matcher mreREWX = reREWX.matcher(token);
 if(mreREWX.matches())
 {
 add_output.append("Since the previous observation (but not at present), the following\\nmeteorological phenomena were observed: ");
 //String myArray = reREWX.exec(token);
 for(int i=1;i<myArray.length; i++)
 {
 if(myArray[i].equals("-")) add_output.append("Light ");
 if(myArray[i].equals("+")) add_output.append("Strong ");
 if(myArray[i].equals("VC")) add_output.append("In the vicinity, ");
 if(myArray[i].equals("MI")) add_output.append("Shallow ");
 if(myArray[i].equals("BC")) add_output.append("Patches of ");
 if(myArray[i].equals("SH")) add_output.append("Showers of ");
 if(myArray[i].equals("TS")) add_output.append("Thunderstorms ");
 if(myArray[i].equals("FZ")) add_output.append("Freezing (or super-cooled) ");
 if(myArray[i].equals("PR")) add_output.append("Partial ");
 if(myArray[i].equals("DZ")) add_output.append("Drizzle ");
 if(myArray[i].equals("RA")) add_output.append("Rain ");
 if(myArray[i].equals("SN")) add_output.append("Snow ");
 if(myArray[i].equals("SG")) add_output.append("Snow grains ");
 if(myArray[i].equals("IC")) add_output.append("Ice Crystals ");
 if(myArray[i].equals("PL")) add_output.append("Ice Pellets ");
 if(myArray[i].equals("GR")) add_output.append("Hail ");
 if(myArray[i].equals("GS")) add_output.append("Small hail (< 5 mm diameter) and/or snow pellets ");
 if(myArray[i].equals("BR")) add_output.append("Mist ");
 if(myArray[i].equals("FG")) add_output.append("Fog ");
 if(myArray[i].equals("FU")) add_output.append("Smoke ");
 if(myArray[i].equals("VA")) add_output.append("Volcanic Ash ");
 if(myArray[i].equals("DU")) add_output.append("Widespread dust ");
 if(myArray[i].equals("SA")) add_output.append("Sand ");
 if(myArray[i].equals("HZ")) add_output.append("Haze ");
 if(myArray[i].equals("PO")) add_output.append("Dust/Sand whirls ");
 if(myArray[i].equals("SQ")) add_output.append("Squall ");
 if(myArray[i].equals("FC")) add_output.append("Funnel clouds ");
 if(myArray[i].equals("SS")) add_output.append("Sandstorm ");
 if(myArray[i].equals("DS")) add_output.append("Duststorm ");
 }
 add_output.append("\\n"); return add_output.toString();
 }*/


        // Check if token is temperature / dewpoint pair
        Pattern reTempDew = Pattern.compile("^(M?)(\\d\\d)/(M?)(\\d\\d)?$");
        Matcher mreTempDew = reTempDew.matcher(token);
        if (mreTempDew.matches()) {
            //String myArray = reTempDew.exec(token);

            if (mreTempDew.group(1).length() == 0) {
                temp = Integer.parseInt(mreTempDew.group(2));
            } else {
                temp = -1 * Integer.parseInt(mreTempDew.group(2));
            }


            if (mreTempDew.group(3).length() == 0) {
                dp = Integer.parseInt(mreTempDew.group(4));
            } else {
                dp = -1 * Integer.parseInt(mreTempDew.group(4));
            }

            return;
        }


        // Check if token is "sky clear" indication
        if (token.equals("SKC")) {
            cloudc = 0;
            return;
        }


 /*// Check if token is "vertical visibility" indication
 Pattern reVV = Pattern.compile("^VV(\\d{3}|\\/{3})$");
 Matcher mreVV = reVV.matcher(token);
 if(mreVV.matches())
 {
 // VVddd -- ddd is vertical distance, or /// if unspecified
 //String myArray = reVV.exec(token);
 add_output.append("Sky is obscured -- vertical visibility");
 if(myArray[1].equals("///"))
 add_output.append(" cannot be assessed\\n");
 else
 add_output.append(": " + (100*Integer.parseInt(myArray[1],10)) + " feet\\n");

 return add_output.toString();
 }*/


        // Check if token is cloud indication
        Pattern reCloud = Pattern.compile("^(FEW|SCT|BKN|OVC)(\\d{3})(CB|TCU)?$");
        Matcher mreCloud = reCloud.matcher(token);
        if (mreCloud.matches()) {
            // Clouds: aaadddkk -- aaa indicates amount of sky covered, ddd distance over
            // aerodrome level, and kk the type of cloud.

            if (mreCloud.group(1).equals("FEW")) {
                cloudc = 15;
            } else if (mreCloud.group(1).equals("SCT")) {
                cloudc = 35;
            } else if (mreCloud.group(1).equals("BKN")) {
                cloudc = 60;
            } else if (mreCloud.group(1).equals("OVC")) {
                cloudc = 100;
            }
            return;

            //add_output.append(", at " + (100*Integer.parseInt(myArray[2],10)) + " feet above aerodrome level");

            //if(myArray[3].equals("CB")) add_output.append(", cumulonimbus");
            //else if(myArray[3].equals("TCU")) add_output.append(", towering cumulus");

        }


 /*// Check if token is part of a wind-shear indication
 Pattern reRWY = Pattern.compile("^RWY(\\d{2})(L|C|R)?$");
 Matcher mreRWY = reRWY.matcher(token);
 if(token.equals("WS")) { add_output.append("There is wind-shear in "); return add_output.toString(); }
 else if(token.equals("ALL")) { add_output.append("all "); return add_output.toString(); }
 else if(token.equals("RWY")) { add_output.append("runways\\n"); return add_output.toString(); }
 else if (mreRWY.matches())
 {
 //String myArray = reRWY.exec(token);
 add_output.append("runway "+myArray[1]);
 if(myArray[2].equals("L")) add_output.append(" Left");
 else if(myArray[2].equals("C")) add_output.append(" Central");
 else if(myArray[2].equals("R")) add_output.append(" Right");
 add_output.append("\\n");
 return add_output.toString();
 }*/


 /*// Check if token is no-significant-weather indication
 if(token.equals("NSW"))
 {
 add_output.append("No significant weather phenomena are observed at present\\n");
 return add_output.toString();
 }


 // Check if token is no-significant-clouds indication
 if(token.equals("NSC"))
 {
 add_output.append("No significant clouds are observed below 5000 feet or below the minimum sector altitude (whichever is higher)\\n");
 return add_output.toString();
 }


 // Check if token is part of trend indication
 if(token.equals("BECMG"))
 {
 add_output.append("The following weather phenomena are expected to arise soon:\\n");
 return add_output.toString();
 }
 if(token.equals("TEMPO"))
 {
 add_output.append("The following weather phenomena are expected to arise temporarily:\\n");
 return add_output.toString();
 }
 Pattern reFM = Pattern.compile("^FM(\\d{2})(\\d{2})Z?$");
 Matcher mreFM = reFM.matcher(token);
 if(mreFM.matches())
 {
 //String myArray = reFM.exec(token);
 add_output.append(" From "+myArray[1]+":"+myArray[2]+" UTC, ");
 return add_output.toString();
 }
 Pattern reTL = Pattern.compile("^TL(\\d{2})(\\d{2})Z?$");
 Matcher mreTL = reTL.matcher(token);
 if(mreTL.matches())
 {
 //String myArray = reTL.exec(token);
 add_output.append("Until "+myArray[1]+":"+myArray[2]+" UTC, ");
 return add_output.toString();
 }
 Pattern reAT = Pattern.compile("^AT(\\d{2})(\\d{2})Z?$");
 Matcher mreAT = reAT.matcher(token);
 if(mreAT.matches())
 {
 //String myArray = reAT.exec(token);
 add_output.append("At "+myArray[1]+":"+myArray[2]+" UTC, ");
 return add_output.toString();
 }


 // Check if item is runway state group
 Pattern reRSG = Pattern.compile("^(\\d\\d)(\\d|C|\\/)(\\d|L|\\/)(\\d\\d|RD|\\/)(\\d\\d)$");
 Matcher mreRSG = reRSG.matcher(token);
 if(mreRSG.matches())
 {
 //String myArray = reRSG.exec(token);
 add_output.append("Runway state:\\n");

 // Runway designator (first 2 digits)
 int r = Integer.parseInt(myArray[1],10);
 if(r < 50) add_output.append(" Runway " + myArray[1] + " (or "+myArray[1]+" Left): ");
 else if(r < 88) add_output.append(" Runway " + (r-50) + " Right: ");
 else if(r == 88) add_output.append(" All runways: ");

 // Check if "CLRD" occurs in digits 3-6
 if(token.substring(2,4).equals("CLRD")) add_output.append("clear, ");
 else
 {
 // Runway deposits (third digit)
 if(myArray[2].equals("0")) add_output.append("clear and dry, ");
 else if(myArray[2].equals("1")) add_output.append("damp, ");
 else if(myArray[2].equals("2")) add_output.append("wet or water patches, ");
 else if(myArray[2].equals("3")) add_output.append("rime or frost covered, ");
 else if(myArray[2].equals("4")) add_output.append("dry snow, ");
 else if(myArray[2].equals("5")) add_output.append("wet snow, ");
 else if(myArray[2].equals("6")) add_output.append("slush, ");
 else if(myArray[2].equals("7")) add_output.append("ice, ");
 else if(myArray[2].equals("8")) add_output.append("compacted or rolled snow, ");
 else if(myArray[2].equals("9")) add_output.append("frozen ruts or ridges, ");
 else if(myArray[2].equals("/")) add_output.append("deposit not reported, ");

 // Extent of runway contamination (fourth digit)
 if(myArray[3].equals("1")) add_output.append("contamination 10% or less, ");
 else if(myArray[3].equals("2")) add_output.append("contamination 11% to 25%, ");
 else if(myArray[3].equals("5")) add_output.append("contamination 26% to 50%, ");
 else if(myArray[3].equals("9")) add_output.append("contamination 51% to 100%, ");
 else if(myArray[3].equals("/")) add_output.append("contamination not reported, ");

 // Depth of deposit (fifth and sixth digits)
 if(myArray[4].equals("//")) add_output.append("depth of deposit not reported, ");
 else
 {
 int d = Integer.parseInt(myArray[4],10);
 if(d == 0) add_output.append("deposit less than 1 mm deep, ");
 else if ((d > 0) && (d < 91)) add_output.append("deposit is "+d+" mm deep, ");
 else if (d == 92) add_output.append("deposit is 10 cm deep, ");
 else if (d == 93) add_output.append("deposit is 15 cm deep, ");
 else if (d == 94) add_output.append("deposit is 20 cm deep, ");
 else if (d == 95) add_output.append("deposit is 25 cm deep, ");
 else if (d == 96) add_output.append("deposit is 30 cm deep, ");
 else if (d == 97) add_output.append("deposit is 35 cm deep, ");
 else if (d == 98) add_output.append("deposit is 40 cm or more deep, ");
 else if (d == 99) add_output.append("runway(s) is/are non-operational due to snow, slush, ice, large drifts or runway clearance, but depth of deposit is not reported, ");
 }
 }

 // Friction coefficient or braking action (seventh and eighth digit)
 if(myArray[5].equals("//")) add_output.append("braking action not reported");
 else
 {
 int b = Integer.parseInt(myArray[5],10);
 if(b<91) add_output.append("friction coefficient 0."+myArray[5]);
 else
 {
 if(b == 91) add_output.append("braking action is poor");
 else if(b == 92) add_output.append("braking action is medium/poor");
 else if(b == 93) add_output.append("braking action is medium");
 else if(b == 94) add_output.append("braking action is medium/good");
 else if(b == 95) add_output.append("braking action is good");
 else if(b == 99) add_output.append("braking action figures are unreliable");
 }
 }
 add_output.append("\\n"); return add_output.toString();
 }

 if(token.equals("SNOCLO"))
 {
 add_output.append("Aerodrome is closed due to snow on runways\\n");
 return add_output.toString();
 }

 // Check if item is sea status indication

 Pattern reSea = Pattern.compile("^W(M)?(\\d\\d)\\/S(\\d)");
 Matcher mreSea = reRSG.matcher(token);
 if(mreSea.matches())
 {
 //String myArray = reSea.exec(token);
 add_output.append("Sea surface temperature: ");
 if(myArray[1].equals("M"))
 add_output.append("-");
 add_output.append(Integer.parseInt(myArray[2],10) + " degrees Celsius\\n");

 add_output.append("Sea waves have height: ");
 if(myArray[3].equals("0")) add_output.append("0 m (calm)\\n");
 else if(myArray[3].equals("1")) add_output.append("0-0,1 m\\n");
 else if(myArray[3].equals("2")) add_output.append("0,1-0,5 m\\n");
 else if(myArray[3].equals("3")) add_output.append("0,5-1,25 m\\n");
 else if(myArray[3].equals("4")) add_output.append("1,25-2,5 m\\n");
 else if(myArray[3].equals("5")) add_output.append("2,5-4 m\\n");
 else if(myArray[3].equals("6")) add_output.append("4-6 m\\n");
 else if(myArray[3].equals("7")) add_output.append("6-9 m\\n");
 else if(myArray[3].equals("8")) add_output.append("9-14 m\\n");
 else if(myArray[3].equals("9")) add_output.append("more than 14 m (huge!)\\n");
 return add_output.toString();
 }*/

    }

    private void metar_decode(String text) {
        String metarstring[] = text.split("\\s");

        for (String tolken : metarstring) {

            if (tolken.equalsIgnoreCase(station)) {
                continue;
            }

            if (tolken.equalsIgnoreCase("AUTO")) {
                continue;
            }

            if (tolken.equalsIgnoreCase(MONTH + HOUR + MIN + "Z")) {
                continue;
            }

            Pattern reDate = Pattern.compile("(\\d\\d\\d\\d)/(\\d\\d)/(\\d\\d)");
            Matcher mreDate = reDate.matcher(tolken);
            if (mreDate.matches()) {
                String g1 = mreDate.group(1);
                String g2 = mreDate.group(2);
                String g3 = mreDate.group(3);
                YEAR = Integer.parseInt(g1);
                DAY = Integer.parseInt(g2);
                MONTH = Integer.parseInt(g3);
                continue;
            }

            // Check if initial token is non-METAR time
            Pattern reTime = Pattern.compile("(\\d\\d):(\\d\\d)");
            Matcher mreTime = reTime.matcher(tolken);
            if (mreTime.matches()) {
                HOUR = Integer.parseInt(mreTime.group(1));
                MIN = Integer.parseInt(mreTime.group(2));
                continue;
            }

            decode_token(tolken);


        }
        //Log.i("Finished", getMethodName());

    }

    public void getHazards() {

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
                //menulist.add(readEntry(parser));
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
        String tmpFlavor;

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
        return new MenuMaker(itName, itPrice, itFlavor, itType, 0);
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
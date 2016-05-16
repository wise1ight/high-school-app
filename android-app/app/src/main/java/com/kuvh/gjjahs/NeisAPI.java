package com.kuvh.gjjahs;

import android.content.Context;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

public class NeisAPI {
    private static Source mSource;

    public static String getDiet(Context context, String schMmealScCode, String date) {
        String[] content = new String[7];
        String url = "http://hes.gne.go.kr/sts_sci_md01_001.do?schulCode=S100000470&schulCrseScCode=3&schMmealScCode=" + schMmealScCode + "&schYmd=" + date;

        //요일
        Calendar cal = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        try {
            Date d = df.parse(date);
            cal.setTime(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);

        try {
            mSource = new Source(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSource.fullSequentialParse();
        List<?> table = mSource.getAllElements("table");

        for (int i = 0; i < table.size(); i++) {
            if (((Element) table.get(i)).getAttributeValue("class").equals(
                    "tbl_type3")) {
                List<?> tbody = ((Element) table.get(i))
                        .getAllElements("tbody");
                List<?> tr = ((Element) tbody.get(0)).getAllElements("tr");
                List<?> title = ((Element) tr.get(2)).getAllElements("th");

                if (((Element) title.get(0)).getContent().toString()
                        .equals("식재료")) {
                    List<?> tdMeal = ((Element) tr.get(1)).getAllElements("td");

                    for (int j = 0; j < 7; j++) {
                        content[j] = ((Element) tdMeal.get(j)).getContent()
                                .toString();
                        content[j] = content[j].replace("<br />", ",");
                        //cal.add(Calendar.DATE, 1);
                    }

                    break;
                }

                for (int index = 0; index < content.length; index++) {
                    content[index] = null;
                }

                break;
            }
        }
        return content[cal.get(Calendar.DAY_OF_WEEK) - 1];
    }
}
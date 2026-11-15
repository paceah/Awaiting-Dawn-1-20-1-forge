package com.paceah.awaitingdawn;

public final class Pluralization {
    public static String form(String locale,long number) {
        if("ru_ru".equalsIgnoreCase(locale)) return form(true,number);
        if("pl_pl".equalsIgnoreCase(locale)) {
            if(number==1) return "one";
            long n=Math.abs(number)%100;
            return n%10>=2 && n%10<=4 && !(n>=12 && n<=14)?"few":"many";
        }
        if("fr_fr".equalsIgnoreCase(locale) || "pt_br".equalsIgnoreCase(locale))
            return number==0 || number==1?"one":"many";
        return form(false,number);
    }
    public static String form(boolean russian,long number) {
        if(!russian) return number==1?"one":"many";
        long n=Math.abs(number)%100;
        if(n>=11 && n<=14) return "many";
        return n%10==1?"one":n%10>=2 && n%10<=4?"few":"many";
    }
}

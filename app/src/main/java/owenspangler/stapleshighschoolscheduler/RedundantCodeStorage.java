package owenspangler.stapleshighschoolscheduler;

public class RedundantCodeStorage {
    /*
    String FindDayLetter(){
        Log.i("LOOP??","REACHED STEP 1");
        int dayLetterCounter = 0;//A = 0, B = 1, C = 2, D = 3
        //if((jsonData.equals("")) || (jsonData.equals("NO CONNECTION"))) {//Backup in case of network failure
            //int tempADayReferenceMonth = 4;
            //int tempADayReferenceDay = 10;
            //int tempDayofWeek = 1;//Sunday is day 0, Saturday is day 6
        //}else{
            //normal conditions

            if ((jsonnumoflastKnownADay >= 0) && (jsonnumoflastKnownADay<=7)){

                int tempMonth = jsonlastKnownADayMonth;
                int tempDay = jsonlastKnownADayDay;
                int tempDayNum = jsonnumoflastKnownADay;
                //Calendar tempCal = new Calendar(2018, tempMonth, tempDay);

                Log.i("LOOP??","REACHED STEP 2");

                while((currentMonth != tempMonth) && (currentDayDay != tempDay)){
                    //Log.i("LOOP??","REACHED STEP 3");
                    if(tempDay >= amountOfDaysInMonth(tempMonth)) {
                        tempDay = 1;
                        if (tempMonth == 12) {
                            tempMonth = 1;
                        } else {
                            tempMonth++;
                        }
                        //dayLeterCounter++;
                    }
                    if(tempDayNum == 7){
                    dayLetterCounter++;
                    tempDayNum = 1;
                    }else if ((tempDayNum>=1) && (tempDayNum <=6)){
                        dayLetterCounter++;
                        tempDayNum++;
                    }
                }
            }else{
                Log.e("LAST KNOWN A DAY", "You done goofed and set the last known A day to a weekend. Fix it");
            }
            Log.i("LOOP??","REACHED STEP 4");
        //}
Log.i("A DAY COUNTER", Integer.toString(dayLetterCounter));
        if(dayLetterCounter%4 == 0){
            return "A";
        }else if(dayLetterCounter%4 == 1){
            return "B";
        }else if(dayLetterCounter%4 ==2){
            return "C";
        }else{
            return "D";
        }
    }

    int amountOfDaysInMonth(int inputMonth){
        if((inputMonth == 1)||(inputMonth == 3)||(inputMonth == 5)||(inputMonth == 7)||(inputMonth == 8)||(inputMonth == 10)||(inputMonth == 12)){
            return 31;
        }else if((inputMonth == 4)||(inputMonth == 6)||(inputMonth == 9)||(inputMonth == 11)){
            return 30;
        }else{
            return 28; //Change for leap years
        }
    }
*/

}

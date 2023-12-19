package com.example.whowhot;

import android.util.Log;

public class SenderChecker {
    private static final String TAG = "TEST_SENDER_CHECK";
    private static final int CRD = 4;
    private static final int BNK = 5;
    private static final int GOV = 6;
    private static final int DLV = 7;

    private static final String[][] msgCardPhone = {{"BC카드", "15884000", "15664000"}, {"KB국민카드","15881688"}, {"NH농협카드","16444000"}, {"롯데카드", "15888100"},
            {"삼성카드", "15888700"}, {"신한카드", "15447000"}, {"우리카드", "15889955", "15999955"}, {"하나카드", "18001111"},{"현대카드", "15776000"}};
    private static final String[][] msgDeliveryPhone = {{"CJ대한통운","15881255"},{"우체국택배","15881300"}, {"롯데택배","15882121"}, {"한진택배","15880011","15440011"},
            {"로젠택배","15889988"}, {"경동택배","18995368"}, {"합동택배","18993392"}, {"쿠팡","15777011"}};
    private static final String[][] msgBankPhone= {{"KEB하나은행","15991111","15881111"}, {"경남은행","16008585","15888585"}, {"기업은행","15662566","15882588"}, {"SC제일은행","15881599"},
            {"광주은행","15883388","16004000"}, {"농협","16613000","15223000"}, {"국민은행","15999999","16449999"}, {"대구은행","15665050","15885050"}, {"수협","15881515","16441515"},
            {"신한은행","15778000","15998000","15448000","16448116"}, {"부산은행","15886200","15446200"}, {"한국산업은행","15881500","16881500"}, {"전북은행","15884477"},
            {"우리은행","15885000","15995000"}, {"제주은행","15880079"}};
    private static final String[][] msgGovermentPhone= {{"경찰청교통민원24","182"}, {"관세청","125","15441285"}, {"국방부","15779090"}, {"국세청","126","15449944"}, {"국토교통부","110","15990001"},
            {"병무청","15889090"}, {"보건복지부","129"}, {"대검찰청","0234802000","110","16008172"}, {"환경부","15778866"}, {"국민건강보험","15771000"}, {"금융감독원","1332"}};

    public SenderChecker() {
    }

    public int checkSender(String sender, String content){
        int type=0;

        if(isCardSmishing(sender, content)) {
            Log.d(TAG, "checkSender : 카드 사칭 스미싱!");
            type = CRD;
        }
        else if(isDeliverySmishing(sender, content)){
            Log.d(TAG, "checkSender : 택배 사칭 스미싱!");
            type = DLV;
        }
        else if(isBankSmishing(sender, content)){
            Log.d(TAG, "checkSender : 은행 사칭 스미싱!");
            type = BNK;
        }
        else if(isGovermentSmishing(sender, content)){
            Log.d(TAG, "checkSender : 정부 사칭 스미싱!");
            type = GOV;
        }

        return type;
    }

    // 카드사 사칭 스미싱
    public boolean isCardSmishing(String sender, String content){
        return !isNormalSender(sender, content, msgCardPhone);    // 정상 발신자가 아니면 카드사 사칭임
    }

    // 택배사 사칭 스미싱
    public boolean isDeliverySmishing(String sender, String content){
        return !isNormalSender(sender, content, msgDeliveryPhone);    // 정상 발신자가 아니면 택배사 사칭임
    }

    // 은행 사칭 스미싱
    public boolean isBankSmishing(String sender, String content){
        return !isNormalSender(sender, content, msgBankPhone);    // 정상 발신자가 아니면 은행 사칭임
    }

    // 정부 사칭 스미싱
    public boolean isGovermentSmishing(String sender, String content){
        return !isNormalSender(sender, content, msgGovermentPhone);    // 정상 발신자가 아니면 정부기관 사칭임
    }

    /* 정상적인 발신자인지 확인하는 함수 */
    public boolean isNormalSender(String sender, String content, String[][] officialData){
        String officialSender="none";
        for (String[] list : officialData){
            officialSender = list[0];   // 사칭할수 있는 기관명
            if (content.contains(officialSender)){ // 만약 문자 중에 사칭할수 있는 기관명이 있으면
                Log.d(TAG,"기관명 : " + officialSender);
                for (String phoneNum : list) {  // 저장된 리스트에서
                    if (sender.contains(phoneNum)) { // 발신자 전화번호가 해당 기관의 전화번호와 일치하면 정상 발신자
                        Log.d(TAG, "발신자 번호 : " + phoneNum);
                        return true;
                    }
                } // for문에 걸리지 않으면 기관명과 전화번호가 일치하지 않아 return 되지 않았으므로 사칭
                Log.d(TAG, "정상적이지 않은 발신자. " + officialSender + " 사칭으로 판별");
                return false;
            }
        }
        Log.d(TAG, "SenderChecker : 발신자 테스트 통과");
        return true;
    }
}

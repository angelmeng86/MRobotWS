package com.maple.rmi;

import java.util.Date;

/**
 * Created by mapple on 2019/1/10.
 */

public interface LocalMessageInterface {
    String sayHello(String name);
    Date getDate();
    
    String pushMessage(String message);
    String forwardMessage(String message);

    String getUserInfo();
    String userLogin(String userName, String pwd);
}

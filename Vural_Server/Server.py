"""Created by Emanuel Amit 5/30/2017 2:44"""
from DatabaseHandler import DatabaseHandler
from Errors import *
from time import *
import random
import re
import socket
from send_text import SendText


NEW_USER_INIT_MESSAGE_TYPE = "0"
NEW_USER_FINAL_MESSAGE_TYPE = "1"
CREATE_CHAT_MESSAGE_TYPE = "2"
DELETE_CHAT_MESSAGE_TYPE = "3"
RENAME_CHAT_MESSAGE_TYPE = "4"
ADD_USER_TO_CHAT_MESSAGE_TYPE = "5"
REMOVE_USER_FROM_CHAT_MESSAGE_TYPE = "6"
SEND_MESSAGE_TYPE = "7"
GET_MESSAGE_TYPE = "8"
MARK_AS_READ_MESSAGE_TYPE = "9"
ADD_USER_LIST_TO_CHAT_MESSAGE_TYPE = "10"
CREATE_PRIVATE_CHAT_MESSAGE_TYPE = "11"
GET_CHAT_NAME_MESSAGE_TYPE = "12"

DB = DatabaseHandler()
SMS = SendText()


def get_field(field_name, string):
    """
    Finds field in string. Field - "|user_id=1234"
    :param field_name: name of field to find
    :param string: string to extract field data from
    :return: field data
    """
    try:
        return re.search("\|" + field_name + "=([^|]+)", string).group(1)
    except Exception as e:
        raise ServerError("Field does not exist - " + field_name)


def main():
    # create a socket object
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    serversocket.bind(("0.0.0.0", 3626))
    serversocket.listen(1)

    while True:
        # establish a connection
        clientsocket, addr = serversocket.accept()

        # set t to start time
        t = time()

        print("Got a connection from " + str(addr))
        message = clientsocket.recv(1024)
        print "Message - " + message

        try:
            # get message type
            message_type = get_field("message_type", message)
            handle_message(message_type, message)
        except ServerError as e:
            # catch server errors
            print e.get_message()
            clientsocket.sendall("|message_type=-1|message=" + e.get_message() + "\n")
        except ServerMessage as m:
            # catch server messages
            print m.get_message()

            clientsocket.sendall("|message_type=" + message_type + "|message=" + m.get_message() + "\n")

        clientsocket.close()

        # print time to handle message
        print "Time - " + str(time()-t)


def handle_message(message_type, message):

    # Messages without user_id
    if message_type == NEW_USER_INIT_MESSAGE_TYPE:  # NEW USER INIT
        new_user_init(message)

    elif message_type == NEW_USER_FINAL_MESSAGE_TYPE:  # NEW USER FINAL
        new_user_final(message)

    elif message_type == GET_CHAT_NAME_MESSAGE_TYPE:  # GET CHAT NAME
        get_chat_name(message)

    # Get user id from message
    user_id = get_field("user_id", message)

    # Raise error if user does not exist
    if not DB.check_if_user_id_exists(user_id):
        raise ServerError("User does not exist - " + user_id)

    # Messages only with user_id
    if message_type == CREATE_CHAT_MESSAGE_TYPE:  # CREATE CHAT
        create_chat(message, user_id)

    if message_type == CREATE_PRIVATE_CHAT_MESSAGE_TYPE:  # CREATE PRIVATE CHAT
        create_private_chat(message, user_id)

    elif message_type == GET_MESSAGE_TYPE:  # GET MESSAGES
        get_unread_messages(user_id)

    # Get chat id from message
    chat_id = get_field("chat_id", message)

    # Raise error if chat does not exist
    if not DB.check_if_chat_id_exists(chat_id):
        raise ServerError("Chat id does not exist - " + chat_id)

    # Messages with chat_id
    if message_type == DELETE_CHAT_MESSAGE_TYPE:  # DELETE CHAT
        delete_chat(user_id, chat_id)

    elif message_type == SEND_MESSAGE_TYPE:  # SEND MESSAGE
        send_message(message, user_id, chat_id)

    elif message_type == RENAME_CHAT_MESSAGE_TYPE:  # RENAME CHAT
        # TODO rename chat
        pass

    elif message_type == ADD_USER_TO_CHAT_MESSAGE_TYPE:  # ADD USER TO
        add_user_to_chat(message, user_id, chat_id)

    elif message_type == ADD_USER_LIST_TO_CHAT_MESSAGE_TYPE:
        add_user_list_to_chat(message, user_id, chat_id)

    elif message_type == REMOVE_USER_FROM_CHAT_MESSAGE_TYPE:
        remove_user_from_chat(message, user_id, chat_id)

    elif message_type == MARK_AS_READ_MESSAGE_TYPE:
        mark_read_messages(message, user_id, chat_id)

    raise ServerError("Message type invalid")


def format_phone_number(phone_number):
    """
    Formats phone number to 972.......
    """

    # TODO support for american numbers
    formatted_phone_number = ""

    # +972.....
    if phone_number[:4] == "+972" and len(phone_number) == 13:
        formatted_phone_number = phone_number[1:]

    # 972......
    elif phone_number[:3] == "972" and len(phone_number) == 12:
        formatted_phone_number = phone_number

    # 05....
    elif phone_number[:2] == "05" and len(phone_number) == 10:
        formatted_phone_number = "972" + phone_number[1:]

    # check that is all digits
    if formatted_phone_number.isdigit():
        return formatted_phone_number

    raise ServerError("invalid phone number - " + phone_number)


def get_phone_number(message):
    """ extract phone number from message """
    phone_number = get_field("phone_number", message)

    return format_phone_number(phone_number)


def new_user_init(message):
    """
    Begin new user process -
        Check if phone number taken
        Check if username taken
        Add to new user database
        Send validation code to phone
    params
        phone_number
        username
    returns:
        error - user exists for phone number
        error - username exists
        error - new user data exists for phone number
        success
            verification code
    """

    phone_number = get_phone_number(message)
    username = get_field("username", message)

    # if user exists raise error
    if DB.check_if_phone_number_exists(phone_number):
        raise ServerError("User exists for phone_number - " + phone_number)

    # if username exists raise error
    if DB.check_if_username_exists(username):
        raise ServerError("Username Exists")

    verification_code = generate_random_number_as_string(4)

    send_verification_code(phone_number, verification_code)

    try:
        # add new user temp details to database
        DB.create_temp_new_user(phone_number, verification_code, username)
    except Exception as e:
        raise ServerError("New user exists for phone number")

    raise ServerMessage("Verification Code Sent")


def generate_random_number_as_string(length):
    """
    Generates a random string at given length. All digits between 1 and 9.
    :param length: length of number to generate
    :return: generated number
    """

    num = ""
    for i in range(length):
        num += str(random.randrange(1, 10))

    return num


def new_user_final(message):
    """
    Finish new user process
        validate verification code
        check time
    params:
        verification code
        phone number
    returns:
        error: no new user data
        error: incorrect code
        error: verification timed out
        successful
            user_id
        
    """

    phone_number = get_phone_number(message)

    verification_code_received = get_field("verification_code", message)

    try:
        verification_code = DB.get_new_user(phone_number)[0]
    except Exception as e:
        raise ServerError("No new user data for phone number")
        print e

    # compare codes
    if not verification_code_received == verification_code[1]:
        update_verification_code(phone_number)
        raise ServerError("Incorrect code new code sent")

    # check if code timed out
    if verification_code[2] <= time():
        update_verification_code(phone_number)
        raise ServerError("Verification timed out new code sent")

    # get new user id for user
    user_id = get_new_user_id()

    # create user in database
    DB.create_user(user_id, phone_number, verification_code[3])

    # remove new user row
    DB.remove_new_user(phone_number)

    # send user_id to user
    raise ServerMessage("User created successfully|user_id=" + user_id)


def get_chat_name(message):
    """
    get chat name for chat id
    params:
        chat id
    returns:
        chat name
    """
    chat_id = get_field("chat_id", message)

    chat_name = DB.get_chat_name(chat_id)

    raise ServerMessage("chat_name=" + chat_name)


def update_verification_code(phone_number):
    """
    Generate new verification code and insert it into database
    :param phone_number: user phone number
    :return: new verification code
    """
    # Get new verification code
    verification_code = generate_random_number_as_string(4)
    # Insert into database
    DB.update_verification_code(phone_number, verification_code)

    send_verification_code(phone_number, verification_code)


def check_if_user_is_in_chat(user_id, chat_id):
    chat_list = DB.get_user_chat_list(user_id)

    # Convert chat id to int
    chat_id_int = int(chat_id)

    if chat_id_int in chat_list:
        return True

    return False


def send_message(message, src_id, chat_id):
    """
    Adds a message to the database
    params:
        src id
        chat id
        lat
        lng
        data
    return:
        message id
        Error - user not in chat
    """

    lat = get_field("lat", message)
    lng = get_field("lng", message)
    message_data = get_field("message_data", message)

    message_id = DB.send_message(src_id, chat_id, lat, lng, message_data)

    raise ServerMessage("|message_id=" + str(message_id))


def mark_read_messages(message, user_id, chat_id):
    """
    Marks messages as read
    params:
        user id
        chat id
        last_read_message_id
    returns:
        successful
        Error - user not in chat
    """

    last_read_message_id = get_field("last_read_message_id", message)

    DB.mark_read_messages(user_id, chat_id, last_read_message_id)

    raise ServerMessage("|message=messages marked")


def get_unread_messages(user_id):
    """
    Get all unread messages for user
    params:
        user id
    return:
        unread messages
    """
    unread_messages = DB.get_all_unread_messages(user_id)
    return_string = ""

    new_chat_names = DB.get_new_chat_names(user_id)

    if len(new_chat_names) != 0:
        return_string += "|added_chats=" + ",".join(new_chat_names)

    return_string += "|unread_messages="

    for message in unread_messages:
        # replace user id in tuple with phone number
        message = message[:2] + (DB.get_user_username(message[2]),) + message[3:7]
        # add message to string. ||| separates between messages. | separates between fields.
        return_string += "|||" + ("|".join(str(s) for s in message))

    raise ServerMessage(return_string)


def get_new_user_id():
    """Get random string of length 10 for message id"""
    while True:
        user_id = generate_random_number_as_string(10)

        # check if user id exists
        if not DB.check_if_user_id_exists(user_id):
            break

    return user_id


def get_new_chat_id():
    """Get random string of length 10 for chat id"""
    while True:
        chat_id = generate_random_number_as_string(10)

        # check if chat id exists
        if not DB.check_if_chat_id_exists(chat_id):
            break

    return chat_id


def create_private_chat(message, user_id):
    """
    Checks if user exists then creates chat
    params:
        user_id
        chat_name
        add_user_phone_number
    returns:
        chat_id
        Error - user does not exist
    """

    # get phone number
    add_user_phone_number = get_field("add_user_phone_number", message)

    # check if user exists
    if not DB.check_if_phone_number_exists(add_user_phone_number):
        raise ServerError("user does not exist")

    # intercept chat id
    try:
        create_chat(message, user_id)
    except ServerMessage as m:
        chat_id = get_field("chat_id", m.get_message())
        # get chat id for user
        add_user_id = DB.get_user_id(add_user_phone_number)
        # add user to chat
        DB.add_user_to_chat(str(add_user_id), chat_id)

    # send chat id to user
    raise m


def create_chat(message, user_id):
    """
    Creates a new chat
    params:
        user_id
        chat_name
    returns:
        successful
            chat_id
    """

    chat_id = get_new_chat_id()
    chat_name = get_field("chat_name", message)

    DB.create_chat(chat_id, chat_name, user_id)
    DB.add_user_to_chat(user_id, chat_id)

    raise ServerMessage("|chat_id=" + chat_id)


def delete_chat(user_id, chat_id):
    """
    Delete a chat
    params:
        user_id
        chat_id
    returns:
        successful
        Error - chat does not exist
        Error - user does not have permissions
    """

    check_if_chat_admin(user_id, chat_id)
    DB.delete_chat(chat_id)

    raise ServerMessage("Chat Deleted - " + chat_id)


def add_user_to_chat(message, user_id, chat_id):
    """
    Add user to chat
    params:
        user_id
        chat_id
        add_user_phone_number
    returns:
        successful
        Error - user does not exist
        Error - user does not have permission
        Error - user already in chat
    """

    check_if_chat_admin(user_id, chat_id)

    add_user_phone_number = get_field("add_user_phone_number", message)

    # check if user exists
    if not DB.check_if_phone_number_exists(add_user_phone_number):
        raise ServerError("User does not exist - " + add_user_phone_number)

    # convert phone number to user id
    add_user_id = DB.get_user_id(add_user_phone_number)

    # check if user is in chat
    if DB.check_if_user_is_in_chat(add_user_id, chat_id):
        raise ServerError("User - " + add_user_id + " already in chat - " + chat_id)

    DB.add_user_to_chat(add_user_id, chat_id)

    raise ServerMessage("User - " + add_user_id + " added to chat - " + chat_id)


def add_user_list_to_chat(message, user_id, chat_id):
    """
    Add a list of users to a chat
    params:
        user_id
        chat_id
        user_list
    returns:
        Successful
            users_added - csv
            users_dont_exist - csv
    """

    check_if_chat_admin(user_id, chat_id)

    user_list = get_field("user_list", message)
    user_list = user_list.split(",")

    users_added = []
    users_dont_exist = []

    for add_user_phone_number in user_list:
        # check if user exists
        if not DB.check_if_phone_number_exists(add_user_phone_number):
            users_dont_exist.append(add_user_phone_number)
            continue

        # get user id for phone number
        add_user_id = str(DB.get_user_id(add_user_phone_number))

        # check if user is in chat
        if DB.check_if_user_is_in_chat(add_user_id, chat_id):
            continue

        DB.add_user_to_chat(add_user_id, chat_id)
        users_added.append(add_user_phone_number)

    raise ServerMessage("|users_added=" + ",".join(users_added) + "|users_dont_exist=" + ",".join(users_dont_exist))


def check_if_chat_admin(user_id, chat_id):
    """
    Checks if user is admin of chat
    params:
        user_id
        chat_id
    returns:
        Error - user not admin
    """
    chat_admin_id = str(DB.get_chat_admin_user_id(chat_id))

    if not chat_admin_id == user_id:
        raise ServerError("User does not have permissions")


def remove_user_from_chat(message, user_id, chat_id):
    """
    removes user from chat
    params:
    """
    check_if_chat_admin(user_id, chat_id)

    remove_user_id = get_field("remove_user_id", message)
    if not DB.check_if_user_id_exists(user_id):
        raise ServerError("User does not exist - " + remove_user_id)

    if not DB.check_if_user_is_in_chat(remove_user_id, chat_id):
        raise ServerError("User - " + remove_user_id + " not in chat - " + chat_id)

    DB.remove_user_from_chat(remove_user_id, chat_id)

    raise ServerMessage("User - " + remove_user_id + " removed from chat - " + chat_id)


def send_verification_code(phone_number, verification_code):
    """sent verification code to user"""

    # only send SMS if phone number is my number because I am running a twilio trial account. If you want to add
    # numbers got to https://www.twilio.com/console/phone-numbers/verified
    if phone_number == "972525403223":
        SMS.send_verification_number(phone_number, verification_code)

    print "verification for - " + phone_number + " = " + verification_code

if __name__ == '__main__':
    t = time()
    main()
    print time() - t

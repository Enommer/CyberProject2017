"""
Created  By Emanuel Amit On 5/18/2017
"""
from send_text_lib.rest import Client


class SendText:

    def __init__(self):
        self.client = Client("AC68ca70c89462adf8f1a331d883ef206f", "6f6a5c53aa52db02af01d94a171cdd27")

    def send_verification_number(self, phone_number, verification_code):
        self.client.api.account.messages.create(to="+"+phone_number,
                                                from_="+12692053411",
                                                body="Your verification code for is - " +
                                                     verification_code)

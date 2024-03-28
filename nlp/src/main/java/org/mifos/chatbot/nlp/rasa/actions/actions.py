# This files contains your custom actions which can be used to run
# custom Python code.
#
# See this guide on how to implement these action:
# https://rasa.com/docs/rasa/custom-actions


# This is a simple example for a custom action which utters "Hello World!"

from typing import Any, Text, Dict, List

from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
class ActionHelloWorld(Action):

    def name(self) -> Text:
        return "action_get_client_id"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        clientId= tracker.get_slot("client_id_slot")
        if not clientId:
            dispatcher.utter_message(text="Sorry I don't get the client id")
        else:
            dispatcher.utter_message(text=f"Your client id is {clientId}")

        return []


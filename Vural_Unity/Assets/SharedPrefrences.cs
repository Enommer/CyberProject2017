using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System;

public class SharedPrefrences : MonoBehaviour
{
    TextMesh text;
    public GameObject ufo;
    public GameObject falcon;
    public GameObject hermes;


    // Use this for initialization
    void Start()
    {
        text = (TextMesh)gameObject.GetComponent(typeof(TextMesh));
        loadValuesFromSharedPrefrences();
    }

    // Update is called once per frame
    void Update()
    {

    }

    public void saveOnDataFromShared(string name)
    {
        PlayerPrefs.SetString("message_text", name);
    }

    public void loadValuesFromSharedPrefrences()
    {
        text.text = PlayerPrefs.GetString("message_text", "Default value");
 
        String modelId = PlayerPrefs.GetString("model_id", "0");
        String lngString = PlayerPrefs.GetString("message_lng", "0.0");
        String latString = PlayerPrefs.GetString("message_lat", "0.0");

        switch (modelId)
        {
            case "0":
                ufo.SetActive(true);
                break;
            case "1":
                falcon.SetActive(true);
                break;
            case "2":
                hermes.SetActive(true);
                break;
        }

        AugmentedScript.originalLongitude = (float)Convert.ToDouble(lngString);
        AugmentedScript.originalLatitude = (float)Convert.ToDouble(latString);
    }
}

package co.yodo.serverconnection;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {
	Boolean currentElement = false;
	String currentValue = null;
	
	public static HashMap<String, String> responseValues = null;

	/**
     *  Called when tag starts
     */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String attr = "";
		
		currentElement = true;
		if (localName.equalsIgnoreCase(ServerResponse.SUB_ROOT_ELEMENT))
		{
			/** Start */
			responseValues = new HashMap<String, String>();
		} 
		else if (localName.equalsIgnoreCase(ServerResponse.CODE_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.CODE_ELEM);
			responseValues.put(ServerResponse.CODE_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.AUTH_NUM_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.AUTH_NUM_ELEM);
			responseValues.put(ServerResponse.AUTH_NUM_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.MESSAGE_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.MESSAGE_ELEM);
			responseValues.put(ServerResponse.MESSAGE_ELEM, attr);
		}
		// response parameters
		else if (localName.equalsIgnoreCase(ServerResponse.BALANCE_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.BALANCE_ELEM);
			responseValues.put(ServerResponse.BALANCE_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.DESCRIPTION_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.DESCRIPTION_ELEM);
			responseValues.put(ServerResponse.DESCRIPTION_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.CREATED_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.CREATED_ELEM);
			responseValues.put(ServerResponse.CREATED_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.AMOUNT_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.AMOUNT_ELEM);
			responseValues.put(ServerResponse.AMOUNT_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.TENDER_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.TENDER_ELEM);
			responseValues.put(ServerResponse.TENDER_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.CASHBACK_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.CASHBACK_ELEM);
			responseValues.put(ServerResponse.CASHBACK_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.RECEIVE_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.RECEIVE_ELEM);
			responseValues.put(ServerResponse.RECEIVE_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.PARAMS_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.PARAMS_ELEM); 
			responseValues.put(ServerResponse.PARAMS_ELEM, attr);
		}
	}

	/** Called when tag closing ( ex:- <name>AndroidPeople</name>
	* -- </name> )*/
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentElement = false;
	
		/** set value */
		if (localName.equalsIgnoreCase(ServerResponse.CODE_ELEM)) {
			/** Get attribute value */
			responseValues.put(ServerResponse.CODE_ELEM, currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.AUTH_NUM_ELEM)){
			/** Get attribute value */
			responseValues.put(ServerResponse.AUTH_NUM_ELEM, currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.MESSAGE_ELEM)){
			/** Get attribute value */
			responseValues.put(ServerResponse.MESSAGE_ELEM, currentValue);
		}
		// response parameters
		else if (localName.equalsIgnoreCase(ServerResponse.BALANCE_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.BALANCE_ELEM, currentValue);
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.BALANCE_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.BALANCE_ELEM+ServerResponse.VALUE_SEPARATOR+currentValue);
		}
		else if(localName.equalsIgnoreCase(ServerResponse.DESCRIPTION_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.DESCRIPTION_ELEM, currentValue);
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.DESCRIPTION_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.DESCRIPTION_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
		}
		else if(localName.equalsIgnoreCase(ServerResponse.CREATED_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.CREATED_ELEM, currentValue);
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.CREATED_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.CREATED_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
		}
		else if(localName.equalsIgnoreCase(ServerResponse.AMOUNT_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.AMOUNT_ELEM, currentValue);
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.AMOUNT_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.AMOUNT_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
		}
		else if(localName.equalsIgnoreCase(ServerResponse.TENDER_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.TENDER_ELEM, currentValue);
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.TENDER_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.TENDER_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
		}
		else if(localName.equalsIgnoreCase(ServerResponse.CASHBACK_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.CASHBACK_ELEM, currentValue);
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.CASHBACK_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.CASHBACK_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
		}
		else if(localName.equalsIgnoreCase(ServerResponse.RECEIVE_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.RECEIVE_ELEM, currentValue);
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.RECEIVE_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.RECEIVE_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.PARAMS_ELEM)){
			/** Get attribute value */
			if(currentValue != null && currentValue != ""){
				//check if I have this value already inside any other tag
				String paramsValue = responseValues.get(ServerResponse.PARAMS_ELEM);
				if(paramsValue != null & paramsValue != ""){
					//if we don't have it yet, then we add it
					if(!paramsValue.contains(currentValue))
						responseValues.put(ServerResponse.PARAMS_ELEM, currentValue + ServerResponse.ENTRY_SEPARATOR + paramsValue);
				}
				else 
					responseValues.put(ServerResponse.PARAMS_ELEM, currentValue);
			}
		}
	}

	/**
     * Called to get tag characters
     */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (currentElement) {
			currentValue = new String(ch, start, length);
			currentElement = false;
		}
	}
}

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.theokanning.openai.*;
import com.theokanning.openai.completion.CompletionRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BBCNewsParser {
    private static final String OPENAI_TOKEN = "YOUR TOKEN HERE";
    private static final String RSS_NEWS_FEED = "http://feeds.bbci.co.uk/news/rss.xml";

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {

        OpenAiService service = new OpenAiService(OPENAI_TOKEN, 10000);
        URL rssUrl = new URL(RSS_NEWS_FEED);
        ArrayList<String> shredlines = new ArrayList<>();

        // Parse the RSS feed
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(rssUrl.openStream());

        // Get all item elements from the RSS feed
        NodeList items = doc.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);

            // Get the title and description elements for the item
            String titleElement = item.getElementsByTagName("title").item(0).getTextContent();
            String[] strings = titleElement.split(" ");
            String newHeadline = "";
            for (int j = 0; j < strings.length-1; j++) {
                newHeadline += strings[j] + " ";
            }
            String finalNewHeadline = newHeadline;

            CompletionRequest completionRequest = CompletionRequest.builder()
                    .prompt("Write a one-sentence news article blurb for the following headline: " + newHeadline)
                    .model("text-davinci-003")
                    .echo(false)
                    .maxTokens(500)
                    .build();
            service.createCompletion(completionRequest).getChoices().forEach(
                    e -> shredlines.add(finalNewHeadline + "\n" + e.getText()));

        }

        shredlines.forEach(System.out::println);
    }
}
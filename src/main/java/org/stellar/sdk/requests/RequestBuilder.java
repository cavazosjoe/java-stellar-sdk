package org.stellar.sdk.requests;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.AssetTypeNative;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for request builders.
 */
public abstract class RequestBuilder {
  protected HttpUrl.Builder uriBuilder;
  protected OkHttpClient httpClient;
  private ArrayList<String> segments;
  private boolean segmentsAdded;

  RequestBuilder(OkHttpClient httpClient, HttpUrl serverURI, String defaultSegment) {
    this.httpClient = httpClient;
    uriBuilder = serverURI.newBuilder();
    segments = new ArrayList<String>();
    if (defaultSegment != null) {
      this.setSegments(defaultSegment);
    }
    segmentsAdded = false; // Allow overwriting segments
  }

  protected RequestBuilder setSegments(String... segments) {
    if (segmentsAdded) {
      throw new RuntimeException("URL segments have been already added.");
    }

    segmentsAdded = true;
    // Remove default segments
    this.segments.clear();
    for (String segment : segments) {
      this.segments.add(segment);
    }

    return this;
  }

  /**
   * Sets <code>cursor</code> parameter on the request.
   * A cursor is a value that points to a specific location in a collection of resources.
   * The cursor attribute itself is an opaque value meaning that users should not try to parse it.
   * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/page.html">Page documentation</a>
   * @param cursor
   */
  public RequestBuilder cursor(String cursor) {
    uriBuilder.setQueryParameter("cursor", cursor);
    return this;
  }

  /**
   * Sets <code>limit</code> parameter on the request.
   * It defines maximum number of records to return.
   * For range and default values check documentation of the endpoint requested.
   * @param number maxium number of records to return
   */
  public RequestBuilder limit(int number) {
    uriBuilder.setQueryParameter("limit", String.valueOf(number));
    return this;
  }

  /**
   * Sets <code>order</code> parameter on the request.
   * @param direction {@link org.stellar.sdk.requests.RequestBuilder.Order}
   */
  public RequestBuilder order(Order direction) {
    uriBuilder.setQueryParameter("order", direction.getValue());
    return this;
  }

  /**
   * Sets a parameter consisting of a comma separated list of assets on the request.
   * @param name the name of the query parameter
   * @param assets the list of assets to be serialized into the query parameter value
   */
  public RequestBuilder setAssetsParameter(String name, List<Asset> assets) {
    List<String> assetStrings = Lists.newArrayList();
    for (Asset asset : assets) {
      if (asset instanceof AssetTypeNative) {
        assetStrings.add("native");
      } else if (asset instanceof AssetTypeCreditAlphaNum) {
        AssetTypeCreditAlphaNum creditAsset = (AssetTypeCreditAlphaNum) asset;
        assetStrings.add(creditAsset.getCode()+":"+creditAsset.getIssuer());
      } else {
        throw new RuntimeException("unsupported asset "+ asset.getType());
      }
    }
    uriBuilder.setQueryParameter(name, Joiner.on(",").join(assetStrings));
    return this;
  }

  HttpUrl buildUri() {
    if (segments.size() > 0) {
      for (String segment : segments) {
        uriBuilder.addPathSegment(segment);
      }
    }
    return uriBuilder.build();
  }

  /**
   * Represents possible <code>order</code> parameter values.
   */
  public enum Order {
    ASC("asc"),
    DESC("desc");
    private final String value;
    Order(String value) {
      this.value = value;
    }
    public String getValue() {
      return value;
    }
  }
}

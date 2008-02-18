
package com.atlassian.theplugin.crucible.api.soap.xfire.review;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.atlassian.theplugin.crucible.api.soap.xfire.review package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ChangeStateResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "changeStateResponse");
    private final static QName _GetReview_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReview");
    private final static QName _AddGeneralCommentResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "addGeneralCommentResponse");
    private final static QName _GetChildReviewsResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getChildReviewsResponse");
    private final static QName _CreateReviewFromPatchResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "createReviewFromPatchResponse");
    private final static QName _RemoveReviewItem_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "removeReviewItem");
    private final static QName _GetChildReviews_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getChildReviews");
    private final static QName _ChangeState_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "changeState");
    private final static QName _GetReviewers_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReviewers");
    private final static QName _GetReviewItemsForReviewResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReviewItemsForReviewResponse");
    private final static QName _AddFisheyeDiffResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "addFisheyeDiffResponse");
    private final static QName _CreateReviewResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "createReviewResponse");
    private final static QName _GetAllReviewsResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getAllReviewsResponse");
    private final static QName _GetAllRevisionComments_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getAllRevisionComments");
    private final static QName _GetVersionedComments_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getVersionedComments");
    private final static QName _GetReviewItemsForReview_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReviewItemsForReview");
    private final static QName _GetReviewsInStatesResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReviewsInStatesResponse");
    private final static QName _AddComment_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "addComment");
    private final static QName _GetGeneralComments_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getGeneralComments");
    private final static QName _AddFisheyeDiff_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "addFisheyeDiff");
    private final static QName _GetReviewersResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReviewersResponse");
    private final static QName _GetReviewResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReviewResponse");
    private final static QName _AddGeneralComment_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "addGeneralComment");
    private final static QName _AddCommentResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "addCommentResponse");
    private final static QName _GetAllReviews_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getAllReviews");
    private final static QName _GetGeneralCommentsResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getGeneralCommentsResponse");
    private final static QName _GetVersionedCommentsResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getVersionedCommentsResponse");
    private final static QName _GetAllRevisionCommentsResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getAllRevisionCommentsResponse");
    private final static QName _CreateReviewFromPatch_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "createReviewFromPatch");
    private final static QName _CreateReview_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "createReview");
    private final static QName _RemoveReviewItemResponse_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "removeReviewItemResponse");
    private final static QName _GetReviewsInStates_QNAME = new QName("http://rpc.spi.crucible.atlassian.com/", "getReviewsInStates");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.atlassian.theplugin.crucible.api.soap.xfire.review
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CreateReviewFromPatch }
     * 
     */
    public CreateReviewFromPatch createCreateReviewFromPatch() {
        return new CreateReviewFromPatch();
    }

    /**
     * Create an instance of {@link AddGeneralComment }
     * 
     */
    public AddGeneralComment createAddGeneralComment() {
        return new AddGeneralComment();
    }

    /**
     * Create an instance of {@link AddCommentResponse }
     * 
     */
    public AddCommentResponse createAddCommentResponse() {
        return new AddCommentResponse();
    }

    /**
     * Create an instance of {@link GetGeneralComments }
     * 
     */
    public GetGeneralComments createGetGeneralComments() {
        return new GetGeneralComments();
    }

    /**
     * Create an instance of {@link GetGeneralCommentsResponse }
     * 
     */
    public GetGeneralCommentsResponse createGetGeneralCommentsResponse() {
        return new GetGeneralCommentsResponse();
    }

    /**
     * Create an instance of {@link GetVersionedCommentsResponse }
     * 
     */
    public GetVersionedCommentsResponse createGetVersionedCommentsResponse() {
        return new GetVersionedCommentsResponse();
    }

    /**
     * Create an instance of {@link GetReviewResponse }
     * 
     */
    public GetReviewResponse createGetReviewResponse() {
        return new GetReviewResponse();
    }

    /**
     * Create an instance of {@link AddFisheyeDiff }
     * 
     */
    public AddFisheyeDiff createAddFisheyeDiff() {
        return new AddFisheyeDiff();
    }

    /**
     * Create an instance of {@link GetReview }
     * 
     */
    public GetReview createGetReview() {
        return new GetReview();
    }

    /**
     * Create an instance of {@link PermId }
     * 
     */
    public PermId createPermId() {
        return new PermId();
    }

    /**
     * Create an instance of {@link ChangeState }
     * 
     */
    public ChangeState createChangeState() {
        return new ChangeState();
    }

    /**
     * Create an instance of {@link GetAllReviewsResponse }
     * 
     */
    public GetAllReviewsResponse createGetAllReviewsResponse() {
        return new GetAllReviewsResponse();
    }

    /**
     * Create an instance of {@link GetAllReviews }
     * 
     */
    public GetAllReviews createGetAllReviews() {
        return new GetAllReviews();
    }

    /**
     * Create an instance of {@link GetReviewsInStates }
     * 
     */
    public GetReviewsInStates createGetReviewsInStates() {
        return new GetReviewsInStates();
    }

    /**
     * Create an instance of {@link GetReviewItemsForReview }
     * 
     */
    public GetReviewItemsForReview createGetReviewItemsForReview() {
        return new GetReviewItemsForReview();
    }

    /**
     * Create an instance of {@link AddGeneralCommentResponse }
     * 
     */
    public AddGeneralCommentResponse createAddGeneralCommentResponse() {
        return new AddGeneralCommentResponse();
    }

    /**
     * Create an instance of {@link AddFisheyeDiffResponse }
     * 
     */
    public AddFisheyeDiffResponse createAddFisheyeDiffResponse() {
        return new AddFisheyeDiffResponse();
    }

    /**
     * Create an instance of {@link GetReviewsInStatesResponse }
     * 
     */
    public GetReviewsInStatesResponse createGetReviewsInStatesResponse() {
        return new GetReviewsInStatesResponse();
    }

    /**
     * Create an instance of {@link GetReviewers }
     * 
     */
    public GetReviewers createGetReviewers() {
        return new GetReviewers();
    }

    /**
     * Create an instance of {@link RemoveReviewItemResponse }
     * 
     */
    public RemoveReviewItemResponse createRemoveReviewItemResponse() {
        return new RemoveReviewItemResponse();
    }

    /**
     * Create an instance of {@link GetVersionedComments }
     * 
     */
    public GetVersionedComments createGetVersionedComments() {
        return new GetVersionedComments();
    }

    /**
     * Create an instance of {@link GetChildReviews }
     * 
     */
    public GetChildReviews createGetChildReviews() {
        return new GetChildReviews();
    }

    /**
     * Create an instance of {@link GetAllRevisionCommentsResponse }
     * 
     */
    public GetAllRevisionCommentsResponse createGetAllRevisionCommentsResponse() {
        return new GetAllRevisionCommentsResponse();
    }

    /**
     * Create an instance of {@link GetReviewItemsForReviewResponse }
     * 
     */
    public GetReviewItemsForReviewResponse createGetReviewItemsForReviewResponse() {
        return new GetReviewItemsForReviewResponse();
    }

    /**
     * Create an instance of {@link CreateReviewResponse }
     * 
     */
    public CreateReviewResponse createCreateReviewResponse() {
        return new CreateReviewResponse();
    }

    /**
     * Create an instance of {@link GetReviewersResponse }
     * 
     */
    public GetReviewersResponse createGetReviewersResponse() {
        return new GetReviewersResponse();
    }

    /**
     * Create an instance of {@link GetChildReviewsResponse }
     * 
     */
    public GetChildReviewsResponse createGetChildReviewsResponse() {
        return new GetChildReviewsResponse();
    }

    /**
     * Create an instance of {@link CreateReviewFromPatchResponse }
     * 
     */
    public CreateReviewFromPatchResponse createCreateReviewFromPatchResponse() {
        return new CreateReviewFromPatchResponse();
    }

    /**
     * Create an instance of {@link AddComment }
     * 
     */
    public AddComment createAddComment() {
        return new AddComment();
    }

    /**
     * Create an instance of {@link GetAllRevisionComments }
     * 
     */
    public GetAllRevisionComments createGetAllRevisionComments() {
        return new GetAllRevisionComments();
    }

    /**
     * Create an instance of {@link ChangeStateResponse }
     * 
     */
    public ChangeStateResponse createChangeStateResponse() {
        return new ChangeStateResponse();
    }

    /**
     * Create an instance of {@link ReviewData }
     * 
     */
    public ReviewData createReviewData() {
        return new ReviewData();
    }

    /**
     * Create an instance of {@link CreateReview }
     * 
     */
    public CreateReview createCreateReview() {
        return new CreateReview();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeStateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "changeStateResponse")
    public JAXBElement<ChangeStateResponse> createChangeStateResponse(ChangeStateResponse value) {
        return new JAXBElement<ChangeStateResponse>(_ChangeStateResponse_QNAME, ChangeStateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReview }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReview")
    public JAXBElement<GetReview> createGetReview(GetReview value) {
        return new JAXBElement<GetReview>(_GetReview_QNAME, GetReview.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddGeneralCommentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "addGeneralCommentResponse")
    public JAXBElement<AddGeneralCommentResponse> createAddGeneralCommentResponse(AddGeneralCommentResponse value) {
        return new JAXBElement<AddGeneralCommentResponse>(_AddGeneralCommentResponse_QNAME, AddGeneralCommentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChildReviewsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getChildReviewsResponse")
    public JAXBElement<GetChildReviewsResponse> createGetChildReviewsResponse(GetChildReviewsResponse value) {
        return new JAXBElement<GetChildReviewsResponse>(_GetChildReviewsResponse_QNAME, GetChildReviewsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateReviewFromPatchResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "createReviewFromPatchResponse")
    public JAXBElement<CreateReviewFromPatchResponse> createCreateReviewFromPatchResponse(CreateReviewFromPatchResponse value) {
        return new JAXBElement<CreateReviewFromPatchResponse>(_CreateReviewFromPatchResponse_QNAME, CreateReviewFromPatchResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChildReviews }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getChildReviews")
    public JAXBElement<GetChildReviews> createGetChildReviews(GetChildReviews value) {
        return new JAXBElement<GetChildReviews>(_GetChildReviews_QNAME, GetChildReviews.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChangeState }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "changeState")
    public JAXBElement<ChangeState> createChangeState(ChangeState value) {
        return new JAXBElement<ChangeState>(_ChangeState_QNAME, ChangeState.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReviewers }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReviewers")
    public JAXBElement<GetReviewers> createGetReviewers(GetReviewers value) {
        return new JAXBElement<GetReviewers>(_GetReviewers_QNAME, GetReviewers.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReviewItemsForReviewResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReviewItemsForReviewResponse")
    public JAXBElement<GetReviewItemsForReviewResponse> createGetReviewItemsForReviewResponse(GetReviewItemsForReviewResponse value) {
        return new JAXBElement<GetReviewItemsForReviewResponse>(_GetReviewItemsForReviewResponse_QNAME, GetReviewItemsForReviewResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddFisheyeDiffResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "addFisheyeDiffResponse")
    public JAXBElement<AddFisheyeDiffResponse> createAddFisheyeDiffResponse(AddFisheyeDiffResponse value) {
        return new JAXBElement<AddFisheyeDiffResponse>(_AddFisheyeDiffResponse_QNAME, AddFisheyeDiffResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateReviewResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "createReviewResponse")
    public JAXBElement<CreateReviewResponse> createCreateReviewResponse(CreateReviewResponse value) {
        return new JAXBElement<CreateReviewResponse>(_CreateReviewResponse_QNAME, CreateReviewResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllReviewsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getAllReviewsResponse")
    public JAXBElement<GetAllReviewsResponse> createGetAllReviewsResponse(GetAllReviewsResponse value) {
        return new JAXBElement<GetAllReviewsResponse>(_GetAllReviewsResponse_QNAME, GetAllReviewsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllRevisionComments }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getAllRevisionComments")
    public JAXBElement<GetAllRevisionComments> createGetAllRevisionComments(GetAllRevisionComments value) {
        return new JAXBElement<GetAllRevisionComments>(_GetAllRevisionComments_QNAME, GetAllRevisionComments.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetVersionedComments }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getVersionedComments")
    public JAXBElement<GetVersionedComments> createGetVersionedComments(GetVersionedComments value) {
        return new JAXBElement<GetVersionedComments>(_GetVersionedComments_QNAME, GetVersionedComments.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReviewItemsForReview }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReviewItemsForReview")
    public JAXBElement<GetReviewItemsForReview> createGetReviewItemsForReview(GetReviewItemsForReview value) {
        return new JAXBElement<GetReviewItemsForReview>(_GetReviewItemsForReview_QNAME, GetReviewItemsForReview.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReviewsInStatesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReviewsInStatesResponse")
    public JAXBElement<GetReviewsInStatesResponse> createGetReviewsInStatesResponse(GetReviewsInStatesResponse value) {
        return new JAXBElement<GetReviewsInStatesResponse>(_GetReviewsInStatesResponse_QNAME, GetReviewsInStatesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddComment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "addComment")
    public JAXBElement<AddComment> createAddComment(AddComment value) {
        return new JAXBElement<AddComment>(_AddComment_QNAME, AddComment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGeneralComments }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getGeneralComments")
    public JAXBElement<GetGeneralComments> createGetGeneralComments(GetGeneralComments value) {
        return new JAXBElement<GetGeneralComments>(_GetGeneralComments_QNAME, GetGeneralComments.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddFisheyeDiff }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "addFisheyeDiff")
    public JAXBElement<AddFisheyeDiff> createAddFisheyeDiff(AddFisheyeDiff value) {
        return new JAXBElement<AddFisheyeDiff>(_AddFisheyeDiff_QNAME, AddFisheyeDiff.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReviewersResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReviewersResponse")
    public JAXBElement<GetReviewersResponse> createGetReviewersResponse(GetReviewersResponse value) {
        return new JAXBElement<GetReviewersResponse>(_GetReviewersResponse_QNAME, GetReviewersResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReviewResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReviewResponse")
    public JAXBElement<GetReviewResponse> createGetReviewResponse(GetReviewResponse value) {
        return new JAXBElement<GetReviewResponse>(_GetReviewResponse_QNAME, GetReviewResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddGeneralComment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "addGeneralComment")
    public JAXBElement<AddGeneralComment> createAddGeneralComment(AddGeneralComment value) {
        return new JAXBElement<AddGeneralComment>(_AddGeneralComment_QNAME, AddGeneralComment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddCommentResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "addCommentResponse")
    public JAXBElement<AddCommentResponse> createAddCommentResponse(AddCommentResponse value) {
        return new JAXBElement<AddCommentResponse>(_AddCommentResponse_QNAME, AddCommentResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllReviews }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getAllReviews")
    public JAXBElement<GetAllReviews> createGetAllReviews(GetAllReviews value) {
        return new JAXBElement<GetAllReviews>(_GetAllReviews_QNAME, GetAllReviews.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGeneralCommentsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getGeneralCommentsResponse")
    public JAXBElement<GetGeneralCommentsResponse> createGetGeneralCommentsResponse(GetGeneralCommentsResponse value) {
        return new JAXBElement<GetGeneralCommentsResponse>(_GetGeneralCommentsResponse_QNAME, GetGeneralCommentsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetVersionedCommentsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getVersionedCommentsResponse")
    public JAXBElement<GetVersionedCommentsResponse> createGetVersionedCommentsResponse(GetVersionedCommentsResponse value) {
        return new JAXBElement<GetVersionedCommentsResponse>(_GetVersionedCommentsResponse_QNAME, GetVersionedCommentsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllRevisionCommentsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getAllRevisionCommentsResponse")
    public JAXBElement<GetAllRevisionCommentsResponse> createGetAllRevisionCommentsResponse(GetAllRevisionCommentsResponse value) {
        return new JAXBElement<GetAllRevisionCommentsResponse>(_GetAllRevisionCommentsResponse_QNAME, GetAllRevisionCommentsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateReviewFromPatch }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "createReviewFromPatch")
    public JAXBElement<CreateReviewFromPatch> createCreateReviewFromPatch(CreateReviewFromPatch value) {
        return new JAXBElement<CreateReviewFromPatch>(_CreateReviewFromPatch_QNAME, CreateReviewFromPatch.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateReview }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "createReview")
    public JAXBElement<CreateReview> createCreateReview(CreateReview value) {
        return new JAXBElement<CreateReview>(_CreateReview_QNAME, CreateReview.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RemoveReviewItemResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "removeReviewItemResponse")
    public JAXBElement<RemoveReviewItemResponse> createRemoveReviewItemResponse(RemoveReviewItemResponse value) {
        return new JAXBElement<RemoveReviewItemResponse>(_RemoveReviewItemResponse_QNAME, RemoveReviewItemResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReviewsInStates }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rpc.spi.crucible.atlassian.com/", name = "getReviewsInStates")
    public JAXBElement<GetReviewsInStates> createGetReviewsInStates(GetReviewsInStates value) {
        return new JAXBElement<GetReviewsInStates>(_GetReviewsInStates_QNAME, GetReviewsInStates.class, null, value);
    }

}

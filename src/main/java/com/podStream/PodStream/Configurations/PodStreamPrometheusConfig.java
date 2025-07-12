package com.podStream.PodStream.Configurations;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;



/**
 * Configuración de métricas Prometheus para PodStream.
 */
@Configuration
public class PodStreamPrometheusConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter answerCreatedCounter;
    private Counter answerUpdatedCounter;
    private Counter answerDeletedCounter;
    private Counter cartItemsAddedCounter;
    private Counter cartItemsUpdatedCounter;
    private Counter cartItemsRemovedCounter;
    private Counter cartClearedCounter;
    private Counter cartMergedCounter;
    private Counter cartPromotionAppliedCounter;
    private Counter cartErrorsCounter;
    private Counter orderCreatedCounter;
    private Counter orderStatusUpdatedCounter;
    private Counter orderDeletedCounter;
    private Counter orderFetchedCounter;
    private Counter orderErrorsCounter;
    private Counter orderCacheHitCounter;
    private Counter emailSentCounter;
    private Counter emailErrorsCounter;
    private Counter categoryCreatedCounter;
    private Counter categoryUpdatedCounter;
    private Counter categoryDeletedCounter;
    private Counter categoryFetchedCounter;
    private Counter categoryCacheHitCounter;
    private Counter categoryErrorsCounter;
    private Counter interactionCreatedCounter;
    private Counter interactionUpdatedCounter;
    private Counter interactionDeletedCounter;
    private Counter interactionFetchedCounter;
    private Counter interactionCacheHitCounter;
    private Counter interactionErrorsCounter;
    private Counter commentCreatedCounter;
    private Counter commentUpdatedCounter;
    private Counter commentDeletedCounter;
    private Counter commentFetchedCounter;
    private Counter commentCacheHitCounter;
    private Counter commentErrorsCounter;
    private Counter detailsCreatedCounter;
    private Counter detailsUpdatedCounter;
    private Counter detailsDeletedCounter;
    private Counter detailsFetchedCounter;
    private Counter detailsCacheHitCounter;
    private Counter detailsErrorsCounter;
    private Counter monitoringTicketCreatedCounter;
    private Counter monitoringTicketUpdatedCounter;
    private Counter monitoringTicketDeletedCounter;
    private Counter monitoringTicketFetchedCounter;
    private Counter monitoringTicketCacheHitCounter;
    private Counter monitoringTicketErrorsCounter;
    private Counter jiraIssuesCreatedCounter;
    private Counter jiraIssuesUpdatedCounter;
    private Counter orderStatusHistoryCreatedCounter;
    private Counter orderStatusHistoryDeletedCounter;
    private Counter orderStatusHistoryFetchedCounter;
    private Counter orderStatusHistoryCacheHitCounter;
    private Counter orderStatusHistoryErrorsCounter;
    private Counter productCreatedCounter;
    private Counter productUpdatedCounter;
    private Counter productDeletedCounter;
    private Counter productFetchedCounter;
    private Counter productCacheHitCounter;
    private Counter productErrorsCounter;
    private Counter productLowStockCounter;
    private Counter productSyncSuccessCounter;
    private Counter productSyncErrorsCounter;
    private Counter searchSuccessCounter;
    private Counter searchErrorsCounter;
    private Counter searchCacheHitCounter;
    private Counter ratingCreatedCounter;
    private Counter ratingUpdatedCounter;
    private Counter ratingDeletedCounter;
    private Counter ratingFetchedCounter;
    private Counter ratingCacheHitCounter;
    private Counter ratingErrorsCounter;
    private Counter recommendationErrorsCounter;
    private Counter recommendationSuccessCounter;
    private Counter recommendationCacheHitCounter;
    private Counter promotionCreatedCounter;
    private Counter promotionUpdatedCounter;
    private Counter promotionDeletedCounter;
    private Counter promotionFetchedCounter;
    private Counter promotionCacheHitCounter;
    private Counter promotionErrorsCounter;
    private Counter promotionAppliedCounter;
    private Counter supportTicketCreatedCounter;
    private Counter supportTicketUpdatedCounter;
    private Counter supportTicketFetchedCounter;
    private Counter supportTicketCacheHitCounter;
    private Counter supportTicketDeletedCounter;
    private Counter supportTicketErrorsCounter;
    private Counter supportTicketJiraCreatedCounter;
    private Counter supportTicketJiraUpdatedCounter;
    private Counter ticketHistoryCreatedCounter;
    private Counter ticketHistoryFetchedCounter;
    private Counter ticketHistoryCacheHitCounter;
    private Counter ticketHistoryDeletedCounter;
    private Counter ticketHistoryErrorsCounter;

    @PostConstruct
    public void initMetrics() {
        answerCreatedCounter = Counter.builder("podstream_answers_created_total")
                .description("Total number of answers created")
                .register(meterRegistry);
        answerUpdatedCounter = Counter.builder("podstream_answers_updated_total")
                .description("Total number of answers updated")
                .register(meterRegistry);
        answerDeletedCounter = Counter.builder("podstream_answers_deleted_total")
                .description("Total number of answers deleted")
                .register(meterRegistry);
        cartItemsAddedCounter = Counter.builder("podstream_cart_items_added_total")
                .description("Total number of items added to carts")
                .register(meterRegistry);
        cartItemsUpdatedCounter = Counter.builder("podstream_cart_items_updated_total")
                .description("Total number of cart items updated")
                .register(meterRegistry);
        cartItemsRemovedCounter = Counter.builder("podstream_cart_items_removed_total")
                .description("Total number of items removed from carts")
                .register(meterRegistry);
        cartClearedCounter = Counter.builder("podstream_cart_cleared_total")
                .description("Total number of carts cleared")
                .register(meterRegistry);
        cartMergedCounter = Counter.builder("podstream_cart_merged_total")
                .description("Total number of carts merged on login")
                .register(meterRegistry);
        cartPromotionAppliedCounter = Counter.builder("podstream_cart_promotion_applied_total")
                .description("Total number of promotions applied to carts")
                .register(meterRegistry);
        cartErrorsCounter = Counter.builder("podstream_cart_errors_total")
                .description("Total number of cart operation errors")
                .register(meterRegistry);
        orderCreatedCounter = Counter.builder("podstream_orders_created_total")
                .description("Total number of purchase orders created")
                .register(meterRegistry);
        orderStatusUpdatedCounter = Counter.builder("podstream_orders_status_updated_total")
                .description("Total number of purchase order status updates")
                .register(meterRegistry);
        orderDeletedCounter = Counter.builder("podstream_orders_deleted_total")
                .description("Total number of purchase orders deleted")
                .register(meterRegistry);
        orderFetchedCounter = Counter.builder("podstream_orders_fetched_total")
                .description("Total number of purchase orders fetched")
                .register(meterRegistry);
        orderErrorsCounter = Counter.builder("podstream_orders_errors_total")
                .description("Total number of purchase order operation errors")
                .register(meterRegistry);
        orderCacheHitCounter = Counter.builder("podstream_orders_cache_hit_total")
                .description("Total number of order cache hits")
                .register(meterRegistry);
        emailSentCounter = Counter.builder("podstream_emails_sent_total")
                .description("Total number of emails sent")
                .register(meterRegistry);
        emailErrorsCounter = Counter.builder("podstream_emails_errors_total")
                .description("Total number of email sending errors")
                .register(meterRegistry);
        categoryCreatedCounter = Counter.builder("podstream_categories_created_total")
                .description("Total number of categories created")
                .register(meterRegistry);
        categoryUpdatedCounter = Counter.builder("podstream_categories_updated_total")
                .description("Total number of categories updated")
                .register(meterRegistry);
        categoryDeletedCounter = Counter.builder("podstream_categories_deleted_total")
                .description("Total number of categories deleted")
                .register(meterRegistry);
        categoryFetchedCounter = Counter.builder("podstream_categories_fetched_total")
                .description("Total number of categories fetched")
                .register(meterRegistry);
        categoryCacheHitCounter = Counter.builder("podstream_categories_cache_hit_total")
                .description("Total number of category cache hits")
                .register(meterRegistry);
        categoryErrorsCounter = Counter.builder("podstream_categories_errors_total")
                .description("Total number of category operation errors")
                .register(meterRegistry);
        interactionCreatedCounter = Counter.builder("podstream_interactions_created_total")
                .description("Total number of client interactions created")
                .register(meterRegistry);
        interactionUpdatedCounter = Counter.builder("podstream_interactions_updated_total")
                .description("Total number of client interactions updated")
                .register(meterRegistry);
        interactionDeletedCounter = Counter.builder("podstream_interactions_deleted_total")
                .description("Total number of client interactions deleted")
                .register(meterRegistry);
        interactionFetchedCounter = Counter.builder("podstream_interactions_fetched_total")
                .description("Total number of client interactions fetched")
                .register(meterRegistry);
        interactionCacheHitCounter = Counter.builder("podstream_interactions_cache_hit_total")
                .description("Total number of client interaction cache hits")
                .register(meterRegistry);
        interactionErrorsCounter = Counter.builder("podstream_interactions_errors_total")
                .description("Total number of client interaction operation errors")
                .register(meterRegistry);
        commentCreatedCounter = Counter.builder("podstream_comments_created_total")
                .description("Total number of comments created")
                .register(meterRegistry);
        commentUpdatedCounter = Counter.builder("podstream_comments_updated_total")
                .description("Total number of comments updated")
                .register(meterRegistry);
        commentDeletedCounter = Counter.builder("podstream_comments_deleted_total")
                .description("Total number of comments deleted")
                .register(meterRegistry);
        commentFetchedCounter = Counter.builder("podstream_comments_fetched_total")
                .description("Total number of comments fetched")
                .register(meterRegistry);
        commentCacheHitCounter = Counter.builder("podstream_comments_cache_hit_total")
                .description("Total number of comment cache hits")
                .register(meterRegistry);
        commentErrorsCounter = Counter.builder("podstream_comments_errors_total")
                .description("Total number of comment operation errors")
                .register(meterRegistry);
        detailsCreatedCounter = Counter.builder("podstream_details_created_total")
                .description("Total number of order details created")
                .register(meterRegistry);
        detailsUpdatedCounter = Counter.builder("podstream_details_updated_total")
                .description("Total number of order details updated")
                .register(meterRegistry);
        detailsDeletedCounter = Counter.builder("podstream_details_deleted_total")
                .description("Total number of order details deleted")
                .register(meterRegistry);
        detailsFetchedCounter = Counter.builder("podstream_details_fetched_total")
                .description("Total number of order details fetched")
                .register(meterRegistry);
        detailsCacheHitCounter = Counter.builder("podstream_details_cache_hit_total")
                .description("Total number of order details cache hits")
                .register(meterRegistry);
        detailsErrorsCounter = Counter.builder("podstream_details_errors_total")
                .description("Total number of order details operation errors")
                .register(meterRegistry);
        monitoringTicketCreatedCounter = Counter.builder("podstream_monitoring_tickets_created_total")
                .description("Total number of monitoring tickets created")
                .register(meterRegistry);
        monitoringTicketUpdatedCounter = Counter.builder("podstream_monitoring_tickets_updated_total")
                .description("Total number of monitoring tickets updated")
                .register(meterRegistry);
        monitoringTicketDeletedCounter = Counter.builder("podstream_monitoring_tickets_deleted_total")
                .description("Total number of monitoring tickets deleted")
                .register(meterRegistry);
        monitoringTicketFetchedCounter = Counter.builder("podstream_monitoring_tickets_fetched_total")
                .description("Total number of monitoring tickets fetched")
                .register(meterRegistry);
        monitoringTicketCacheHitCounter = Counter.builder("podstream_monitoring_tickets_cache_hit_total")
                .description("Total number of monitoring ticket cache hits")
                .register(meterRegistry);
        monitoringTicketErrorsCounter = Counter.builder("podstream_monitoring_tickets_errors_total")
                .description("Total number of monitoring ticket operation errors")
                .register(meterRegistry);
        jiraIssuesCreatedCounter = Counter.builder("podstream_jira_issues_created_total")
                .description("Total number of Jira issues created")
                .register(meterRegistry);
        jiraIssuesUpdatedCounter = Counter.builder("podstream_jira_issues_updated_total")
                .description("Total number of Jira issues updated")
                .register(meterRegistry);
        orderStatusHistoryCreatedCounter = Counter.builder("podstream_order_status_history_created_total")
                .description("Total number of order status history entries created")
                .register(meterRegistry);
        orderStatusHistoryDeletedCounter = Counter.builder("podstream_order_status_history_deleted_total")
                .description("Total number of order status history entries deleted")
                .register(meterRegistry);
        orderStatusHistoryFetchedCounter = Counter.builder("podstream_order_status_history_fetched_total")
                .description("Total number of order status history entries fetched")
                .register(meterRegistry);
        orderStatusHistoryCacheHitCounter = Counter.builder("podstream_order_status_history_cache_hit_total")
                .description("Total number of order status history cache hits")
                .register(meterRegistry);
        orderStatusHistoryErrorsCounter = Counter.builder("podstream_order_status_history_errors_total")
                .description("Total number of order status history operation errors")
                .register(meterRegistry);
        productCreatedCounter = Counter.builder("podstream_products_created_total")
                .description("Total number of products created")
                .register(meterRegistry);
        productUpdatedCounter = Counter.builder("podstream_products_updated_total")
                .description("Total number of products updated")
                .register(meterRegistry);
        productDeletedCounter = Counter.builder("podstream_products_deleted_total")
                .description("Total number of products deleted")
                .register(meterRegistry);
        productFetchedCounter = Counter.builder("podstream_products_fetched_total")
                .description("Total number of products fetched")
                .register(meterRegistry);
        productCacheHitCounter = Counter.builder("podstream_products_cache_hit_total")
                .description("Total number of product cache hits")
                .register(meterRegistry);
        productErrorsCounter = Counter.builder("podstream_products_errors_total")
                .description("Total number of product errors")
                .register(meterRegistry);
        productLowStockCounter = Counter.builder("podstream_products_low_stock_total")
                .description("Total number of low stock alerts")
                .register(meterRegistry);
        productSyncSuccessCounter = Counter.builder("podstream_products_sync_success_total")
                .description("Total number of successful product synchronizations with Elasticsearch")
                .register(meterRegistry);
        productSyncErrorsCounter = Counter.builder("podstream_products_sync_errors_total")
                .description("Total number of product synchronization errors with Elasticsearch")
                .register(meterRegistry);
        searchSuccessCounter = Counter.builder("podstream_search_success_total")
                .description("Total number of successful search operations")
                .register(meterRegistry);
        searchErrorsCounter = Counter.builder("podstream_search_errors_total")
                .description("Total number of search operation errors")
                .register(meterRegistry);
        searchCacheHitCounter = Counter.builder("podstream_search_cache_hit_total")
                .description("Total number of search cache hits")
                .register(meterRegistry);
        ratingCreatedCounter = Counter.builder("podstream_ratings_created_total")
                .description("Total number of ratings created")
                .register(meterRegistry);
        ratingUpdatedCounter = Counter.builder("podstream_ratings_updated_total")
                .description("Total number of ratings updated")
                .register(meterRegistry);
        ratingDeletedCounter = Counter.builder("podstream_ratings_deleted_total")
                .description("Total number of ratings deleted")
                .register(meterRegistry);
        ratingFetchedCounter = Counter.builder("podstream_ratings_fetched_total")
                .description("Total number of ratings fetched")
                .register(meterRegistry);
        ratingCacheHitCounter = Counter.builder("podstream_ratings_cache_hit_total")
                .description("Total number of rating cache hits")
                .register(meterRegistry);
        ratingErrorsCounter = Counter.builder("podstream_ratings_errors_total")
                .description("Total number of rating errors")
                .register(meterRegistry);
        recommendationErrorsCounter = Counter.builder("podstream_recommendations_errors_total")
                .description("Total number of recommendation errors")
                .register(meterRegistry);
        recommendationSuccessCounter = Counter.builder("podstream_recommendations_success_total")
                .description("Total number of successful recommendation operations")
                .register(meterRegistry);
        recommendationCacheHitCounter = Counter.builder("podstream_recommendations_cache_hit_total")
                .description("Total number of recommendation cache hits")
                .register(meterRegistry);
        promotionCreatedCounter = Counter.builder("podstream_promotions_created_total")
                .description("Total number of promotions created")
                .register(meterRegistry);
        promotionUpdatedCounter = Counter.builder("podstream_promotions_updated_total")
                .description("Total number of promotions updated")
                .register(meterRegistry);
        promotionDeletedCounter = Counter.builder("podstream_promotions_deleted_total")
                .description("Total number of promotions deleted")
                .register(meterRegistry);
        promotionFetchedCounter = Counter.builder("podstream_promotions_fetched_total")
                .description("Total number of promotions fetched")
                .register(meterRegistry);
        promotionCacheHitCounter = Counter.builder("podstream_promotions_cache_hit_total")
                .description("Total number of promotion cache hits")
                .register(meterRegistry);
        promotionErrorsCounter = Counter.builder("podstream_promotions_errors_total")
                .description("Total number of promotion errors")
                .register(meterRegistry);
        promotionAppliedCounter = Counter.builder("podstream_promotions_applied_total")
                .description("Total number of promotions applied to carts")
                .register(meterRegistry);
        supportTicketCreatedCounter = Counter.builder("podstream_support_tickets_created_total")
                .description("Total number of support tickets created")
                .register(meterRegistry);
        supportTicketUpdatedCounter = Counter.builder("podstream_support_tickets_updated_total")
                .description("Total number of support tickets updated")
                .register(meterRegistry);
        supportTicketFetchedCounter = Counter.builder("podstream_support_tickets_fetched_total")
                .description("Total number of support tickets fetched")
                .register(meterRegistry);
        supportTicketCacheHitCounter = Counter.builder("podstream_support_tickets_cache_hit_total")
                .description("Total number of support ticket cache hits")
                .register(meterRegistry);
        supportTicketDeletedCounter = Counter.builder("podstream_support_tickets_deleted_total")
                .description("Total number of support tickets deleted")
                .register(meterRegistry);
        supportTicketErrorsCounter = Counter.builder("podstream_support_tickets_errors_total")
                .description("Total number of support ticket errors")
                .register(meterRegistry);
        supportTicketJiraCreatedCounter = Counter.builder("podstream_support_tickets_jira_created_total")
                .description("Total number of Jira issues created for support tickets")
                .register(meterRegistry);
        supportTicketJiraUpdatedCounter = Counter.builder("podstream_support_tickets_jira_updated_total")
                .description("Total number of Jira issues updated for support tickets")
                .register(meterRegistry);
        ticketHistoryCreatedCounter = Counter.builder("podstream_ticket_history_created_total")
                .description("Total number of ticket history entries created")
                .register(meterRegistry);
        ticketHistoryFetchedCounter = Counter.builder("podstream_ticket_history_fetched_total")
                .description("Total number of ticket history entries fetched")
                .register(meterRegistry);
        ticketHistoryCacheHitCounter = Counter.builder("podstream_ticket_history_cache_hit_total")
                .description("Total number of ticket history cache hits")
                .register(meterRegistry);
        ticketHistoryDeletedCounter = Counter.builder("podstream_ticket_history_deleted_total")
                .description("Total number of ticket history entries deleted")
                .register(meterRegistry);
        ticketHistoryErrorsCounter = Counter.builder("podstream_ticket_history_errors_total")
                .description("Total number of ticket history errors")
                .register(meterRegistry);
    }

    public void incrementAnswerCreated() { answerCreatedCounter.increment(); }
    public void incrementAnswerUpdated() { answerUpdatedCounter.increment(); }
    public void incrementAnswerDeleted() { answerDeletedCounter.increment(); }
    public void incrementCartItemsAdded() { cartItemsAddedCounter.increment(); }
    public void incrementCartItemsUpdated() { cartItemsUpdatedCounter.increment(); }
    public void incrementCartItemsRemoved() { cartItemsRemovedCounter.increment(); }
    public void incrementCartCleared() { cartClearedCounter.increment(); }
    public void incrementCartMerged() { cartMergedCounter.increment(); }
    public void incrementCartPromotionApplied() { cartPromotionAppliedCounter.increment(); }
    public void incrementCartErrors() { cartErrorsCounter.increment(); }
    public void incrementOrderCreated() { orderCreatedCounter.increment(); }
    public void incrementOrderStatusUpdated() { orderStatusUpdatedCounter.increment(); }
    public void incrementOrderDeleted() { orderDeletedCounter.increment(); }
    public void incrementOrderFetched() { orderFetchedCounter.increment(); }
    public void incrementOrderErrors() { orderErrorsCounter.increment(); }
    public void incrementOrderCacheHit() { orderCacheHitCounter.increment(); }
    public void incrementEmailSent() { emailSentCounter.increment(); }
    public void incrementEmailErrors() { emailErrorsCounter.increment(); }
    public void incrementCategoryCreated() { categoryCreatedCounter.increment(); }
    public void incrementCategoryUpdated() { categoryUpdatedCounter.increment(); }
    public void incrementCategoryDeleted() { categoryDeletedCounter.increment(); }
    public void incrementCategoryFetched() { categoryFetchedCounter.increment(); }
    public void incrementCategoryCacheHit() { categoryCacheHitCounter.increment(); }
    public void incrementCategoryErrors() { categoryErrorsCounter.increment(); }
    public void incrementInteractionCreated() { interactionCreatedCounter.increment(); }
    public void incrementInteractionUpdated() { interactionUpdatedCounter.increment(); }
    public void incrementInteractionDeleted() { interactionDeletedCounter.increment(); }
    public void incrementInteractionFetched() { interactionFetchedCounter.increment(); }
    public void incrementInteractionCacheHit() { interactionCacheHitCounter.increment(); }
    public void incrementInteractionErrors() { interactionErrorsCounter.increment(); }
    public void incrementCommentCreated() { commentCreatedCounter.increment(); }
    public void incrementCommentUpdated() { commentUpdatedCounter.increment(); }
    public void incrementCommentDeleted() { commentDeletedCounter.increment(); }
    public void incrementCommentFetched() { commentFetchedCounter.increment(); }
    public void incrementCommentCacheHit() { commentCacheHitCounter.increment(); }
    public void incrementCommentErrors() { commentErrorsCounter.increment(); }
    public void incrementDetailsCreated() { detailsCreatedCounter.increment(); }
    public void incrementDetailsUpdated() { detailsUpdatedCounter.increment(); }
    public void incrementDetailsDeleted() { detailsDeletedCounter.increment(); }
    public void incrementDetailsFetched() { detailsFetchedCounter.increment(); }
    public void incrementDetailsCacheHit() { detailsCacheHitCounter.increment(); }
    public void incrementDetailsErrors() { detailsErrorsCounter.increment(); }
    public void incrementMonitoringTicketCreated() { monitoringTicketCreatedCounter.increment(); }
    public void incrementMonitoringTicketUpdated() { monitoringTicketUpdatedCounter.increment(); }
    public void incrementMonitoringTicketDeleted() { monitoringTicketDeletedCounter.increment(); }
    public void incrementMonitoringTicketFetched() { monitoringTicketFetchedCounter.increment(); }
    public void incrementMonitoringTicketCacheHit() { monitoringTicketCacheHitCounter.increment(); }
    public void incrementMonitoringTicketErrors() { monitoringTicketErrorsCounter.increment(); }
    public void incrementJiraIssuesCreated() { jiraIssuesCreatedCounter.increment(); }
    public void incrementJiraIssuesUpdated() { jiraIssuesUpdatedCounter.increment(); }
    public void incrementOrderStatusHistoryCreated() { orderStatusHistoryCreatedCounter.increment(); }
    public void incrementOrderStatusHistoryDeleted() { orderStatusHistoryDeletedCounter.increment(); }
    public void incrementOrderStatusHistoryFetched() { orderStatusHistoryFetchedCounter.increment(); }
    public void incrementOrderStatusHistoryCacheHit() { orderStatusHistoryCacheHitCounter.increment(); }
    public void incrementOrderStatusHistoryErrors() { orderStatusHistoryErrorsCounter.increment(); }
    public void incrementProductCreated() { productCreatedCounter.increment(); }
    public void incrementProductUpdated() { productUpdatedCounter.increment(); }
    public void incrementProductDeleted() { productDeletedCounter.increment(); }
    public void incrementProductFetched() { productFetchedCounter.increment(); }
    public void incrementProductCacheHit() { productCacheHitCounter.increment(); }
    public void incrementProductErrors() { productErrorsCounter.increment(); }
    public void incrementProductLowStock() { productLowStockCounter.increment(); }
    public void incrementProductSyncSuccess() { productSyncSuccessCounter.increment(); }
    public void incrementProductSyncErrors() { productSyncErrorsCounter.increment(); }
    public void incrementSearchSuccess() { searchSuccessCounter.increment(); }
    public void incrementSearchErrors() { searchErrorsCounter.increment(); }
    public void incrementSearchCacheHit() { searchCacheHitCounter.increment(); }
    public void incrementRatingCreated() { ratingCreatedCounter.increment(); }
    public void incrementRatingUpdated() { ratingUpdatedCounter.increment(); }

    public void incrementRatingDeleted() { ratingDeletedCounter.increment(); }
    public void incrementRatingFetched() { ratingFetchedCounter.increment(); }
    public void incrementRatingCacheHit() { ratingCacheHitCounter.increment(); }
    public void incrementRatingErrors() { ratingErrorsCounter.increment(); }

    public void incrementRecommendationErrors() {
        recommendationErrorsCounter.increment();
    }

    public void incrementRecommendationSuccess() {
        recommendationSuccessCounter.increment();
    }

    public void incrementRecommendationCacheHit() {
        recommendationCacheHitCounter.increment();

    }
    public void incrementPromotionCreated() { promotionCreatedCounter.increment(); }
    public void incrementPromotionUpdated() { promotionUpdatedCounter.increment(); }
    public void incrementPromotionDeleted() { promotionDeletedCounter.increment(); }
    public void incrementPromotionFetched() { promotionFetchedCounter.increment(); }
    public void incrementPromotionCacheHit() { promotionCacheHitCounter.increment(); }
    public void incrementPromotionErrors() { promotionErrorsCounter.increment(); }
    public void incrementSupportTicketCreated() { supportTicketCreatedCounter.increment(); }
    public void incrementSupportTicketUpdated() { supportTicketUpdatedCounter.increment(); }
    public void incrementSupportTicketFetched() { supportTicketFetchedCounter.increment(); }
    public void incrementSupportTicketCacheHit() { supportTicketCacheHitCounter.increment(); }
    public void incrementSupportTicketDeleted() { supportTicketDeletedCounter.increment(); }
    public void incrementSupportTicketErrors() { supportTicketErrorsCounter.increment(); }
    public void incrementSupportTicketJiraCreated() { supportTicketJiraCreatedCounter.increment(); }
    public void incrementSupportTicketJiraUpdated() { supportTicketJiraUpdatedCounter.increment(); }
    public void incrementTicketHistoryCreated() { ticketHistoryCreatedCounter.increment(); }
    public void incrementTicketHistoryFetched() { ticketHistoryFetchedCounter.increment(); }
    public void incrementTicketHistoryCacheHit() { ticketHistoryCacheHitCounter.increment(); }
    public void incrementTicketHistoryDeleted() { ticketHistoryDeletedCounter.increment(); }
    public void incrementTicketHistoryErrors() { ticketHistoryErrorsCounter.increment(); }
}
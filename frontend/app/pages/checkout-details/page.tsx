import CheckoutDetails from "~/common/checkout-details/CheckoutDetails";

export default function CheckoutDetailsPage() {
  document.title = "收益分成记录";

  return (
    <div className="p-6">
      <CheckoutDetails />
    </div>
  );
}

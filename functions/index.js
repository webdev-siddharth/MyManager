const { onDocumentDeleted } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const cloudinary = require("cloudinary").v2;

admin.initializeApp();

cloudinary.config({
  cloud_name: process.env.CLOUDINARY_NAME || "",
  api_key: process.env.CLOUDINARY_KEY || "",
  api_secret: process.env.CLOUDINARY_SECRET || "",
});

exports.onProductDeleted = onDocumentDeleted(
  "users/{uid}/products/{productId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const data = snap.data();
    const publicIds = data.cloudinaryPublicIds || [];

    if (publicIds.length === 0) {
      console.log(`No Cloudinary images to delete for product ${event.params.productId}`);
      return;
    }

    console.log(
      `Deleting ${publicIds.length} Cloudinary image(s) for product ${event.params.productId}`
    );

    const deletePromises = publicIds.map((publicId) =>
      cloudinary.uploader
        .destroy(publicId)
        .then((result) => console.log(`Deleted ${publicId}: ${result.result}`))
        .catch((err) => console.error(`Failed to delete ${publicId}:`, err))
    );

    await Promise.all(deletePromises);
    console.log(`Finished cleaning up images for product ${event.params.productId}`);
  }
);

## Add Image API

- Add images field to Post DTO, which should be an array of strings.
  Those strings will be the name of the image files.
  
- Change Controllers and Services accordingly, if needed.

- Images field is not editable

- Add a new api which accept image files and saves to `resources/img` folder

- The new image upload api takes 1 image each time, and the url should be `/img`.

- Your api should return a UUID, which will be used as the image name.
  
## Add Latest Posts API

- Add a new api to fetch the latest posts, with pagination.

- Api url should be `/post/latest/{pageNumber}`

- You need to research about the best practice of pagination.
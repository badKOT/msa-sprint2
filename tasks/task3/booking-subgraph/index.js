import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';
import axios from 'axios';

const bookingServiceHost = process.env.BOOKING_SERVICE_HOST || 'localhost';
const bookingServicePort = process.env.BOOKING_SERVICE_PORT || '8084';
const bookingServiceBaseUrl = `http://${bookingServiceHost}:${bookingServicePort}/api/bookings`;

const typeDefs = gql`
  type Booking @key(fields: "id") {
    id: ID!
    userId: String!
    hotelId: String!
    promoCode: String
    discountPercent: Int
    hotel: Hotel
  }

  extend type Hotel @key(fields: "id") {
    id: ID! @external
  }

  type Query {
    bookingsByUser(userId: String!): [Booking]
  }

`;

const resolvers = {
  Query: {
    bookingsByUser: async (_, { userId }, { req }) => {
      console.log("got query, headers are %j", req.headers);
      if (req.headers && req.headers['user-id']) {
        if (req.headers['user-id'] === userId) {
          const response = await axios.get(`${bookingServiceBaseUrl}?userId=${userId}`);
          return response.data;
        } else {
          console.error(`Header 'user-id' value does not match query userId parameter.`);
          throw new Error(`Header 'user-id' value does not match query userId parameter.`);
        }
      } else {
        console.error(`Required header 'user-id' not found.`);
        throw new Error(`Required header 'user-id' not found.`);
      }
    },
  },
  Booking: {
    __resolveReference: async ({ }) => {
      return {id: "test-id", userId: "test-user-0", hotelId: "2"}
    },
    hotel: (booking) => {
      return { __typename: "Hotel", id: booking.hotelId };
    }
  },
};

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
});

startStandaloneServer(server, {
  listen: { port: 4001 },
  context: async ({ req }) => ({ req }),
}).then(() => {
  console.log('✅ Booking subgraph ready at http://localhost:4001/');
});
